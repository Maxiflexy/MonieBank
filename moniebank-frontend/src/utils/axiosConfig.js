import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // This is crucial for sending cookies
  timeout: 10000, // 10 second timeout
});

// Add a request interceptor for debugging
axiosInstance.interceptors.request.use(
  (config) => {
    console.log('Making request to:', config.url);
    console.log('Request config:', config);
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// Add a response interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    console.log('Response received:', response.status, response.config.url);
    return response;
  },
  async (error) => {
    console.error('Response error:', error);

    // Log CORS-related errors for debugging
    if (error.response?.status === 403 && error.message.includes('CORS')) {
      console.error('CORS Error Details:', {
        status: error.response.status,
        statusText: error.response.statusText,
        headers: error.response.headers,
        config: error.config
      });
    }

    const originalRequest = error.config;

    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        console.log('Attempting token refresh...');
        // Try to refresh the token
        await axios.post(`${API_BASE_URL}/auth/refresh`, {}, {
          withCredentials: true
        });

        console.log('Token refresh successful, retrying original request');
        // Retry the original request
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        // Refresh failed, redirect to login
        localStorage.removeItem('user');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    if (error.response && error.response.status === 401) {
      console.log('Unauthorized, redirecting to login');
      // Handle unauthorized errors (e.g., redirect to login)
      localStorage.removeItem('user');
      window.location.href = '/login';
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;