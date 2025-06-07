import axios from 'axios';
import {
  encryptionRequestInterceptor,
  decryptionResponseInterceptor,
  decryptionErrorInterceptor
} from './encryptionInterceptors';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // This is crucial for sending cookies
  timeout: 10000, // 10 second timeout
});

// Track refresh attempts to prevent infinite loops
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// SINGLE request interceptor that handles both logging and encryption
axiosInstance.interceptors.request.use(
  async (config) => {
    console.log('Making request to:', config.url);
    // Apply encryption interceptor
    return await encryptionRequestInterceptor(config);
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// SINGLE response interceptor that handles decryption AND token refresh
axiosInstance.interceptors.response.use(
  async (response) => {
    console.log('Response received:', response.status, response.config.url);
    // Apply decryption interceptor first
    return await decryptionResponseInterceptor(response);
  },
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 errors with token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {

      // If we're already refreshing, queue this request
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(async () => {
          // Retry the original request and apply decryption to the response
          const retryResponse = await axiosInstance(originalRequest);
          return await decryptionResponseInterceptor(retryResponse);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        console.log('Attempting token refresh...');

        // Try to refresh the token - this call will also be encrypted/decrypted automatically
        await axios.post(`${API_BASE_URL}/auth/refresh`, {}, {
          withCredentials: true
        });

        console.log('Token refresh successful');
        processQueue(null);

        // Retry the original request and apply decryption
        const retryResponse = await axiosInstance(originalRequest);
        return await decryptionResponseInterceptor(retryResponse);

      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        processQueue(refreshError);

        // Clear user data and redirect to login
        localStorage.removeItem('user');

        // Only redirect if we're not already on the login page
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // Apply decryption to error responses for other types of errors
    return await decryptionErrorInterceptor(error);
  }
);

export default axiosInstance;