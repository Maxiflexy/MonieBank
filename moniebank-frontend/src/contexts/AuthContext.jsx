import React, { createContext, useState, useEffect } from 'react';
import authApi from '../api/authApi';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const loadUser = async () => {
      try {
        // First, try to get current user using existing cookies
        const userData = await authApi.getCurrentUser();
        setCurrentUser(userData);

        // Store user data (but not tokens) in localStorage for UI purposes
        localStorage.setItem('user', JSON.stringify({
          id: userData.id,
          email: userData.email,
          name: userData.name
        }));
      } catch (error) {
        console.error('Failed to load user, attempting refresh:', error);

        // If getCurrentUser fails, try refreshing the token
        try {
          console.log('Attempting token refresh during initialization...');
          await authApi.refreshToken();

          // After successful refresh, try getting user data again
          const userData = await authApi.getCurrentUser();
          setCurrentUser(userData);

          localStorage.setItem('user', JSON.stringify({
            id: userData.id,
            email: userData.email,
            name: userData.name
          }));

          console.log('Token refresh and user load successful');
        } catch (refreshError) {
          console.error('Token refresh failed during initialization:', refreshError);
          // Clear any existing user data
          localStorage.removeItem('user');
          setCurrentUser(null);
        }
      }
      setLoading(false);
    };

    loadUser();
  }, []);

  const login = async (credentials) => {
    try {
      const response = await authApi.login(credentials);

      // Store only user data, cookies are handled automatically
      const userData = {
        id: response.userId,
        email: response.email,
        name: response.name
      };

      localStorage.setItem('user', JSON.stringify(userData));
      setCurrentUser(userData);

      toast.success('Login successful!');
      navigate('/dashboard');
      return response;
    } catch (error) {
      if (error.response && error.response.data) {
        toast.error(error.response.data.message || 'Login failed');
      } else {
        toast.error('Login failed');
      }
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const response = await authApi.register(userData);
      toast.success(response.message || 'Registration successful! Please check your email to verify your account.');
      navigate('/login');
      return response;
    } catch (error) {
      if (error.response && error.response.data) {
        toast.error(error.response.data.message || 'Registration failed');
      } else {
        toast.error('Registration failed');
      }
      throw error;
    }
  };

  const googleLogin = async (tokenId) => {
    try {
      const response = await authApi.googleLogin(tokenId);

      // Store only user data, cookies are handled automatically
      const userData = {
        id: response.userId,
        email: response.email,
        name: response.name
      };

      localStorage.setItem('user', JSON.stringify(userData));
      setCurrentUser(userData);

      toast.success('Login with Google successful!');
      navigate('/dashboard');
      return response;
    } catch (error) {
      toast.error('Login with Google failed');
      throw error;
    }
  };

  const logout = async () => {
    try {
      // Call logout endpoint to clear HTTP-only cookies
      await authApi.logout();
    } catch (error) {
      console.error('Logout API call failed:', error);
      // Continue with logout even if API call fails
    } finally {
      // Clear local storage and state
      localStorage.removeItem('user');
      setCurrentUser(null);
      navigate('/login');
      toast.info('Logged out successfully');
    }
  };

  const updateProfile = async (profileData) => {
    try {
      const response = await authApi.updateProfile(profileData);

      // Update local storage and current user state
      const updatedUser = {
        ...currentUser,
        name: profileData.name,
      };

      localStorage.setItem('user', JSON.stringify(updatedUser));
      setCurrentUser(updatedUser);

      toast.success('Profile updated successfully!');
      return response;
    } catch (error) {
      toast.error('Failed to update profile');
      throw error;
    }
  };

  // Function to refresh user data (useful after token refresh)
  const refreshUser = async () => {
    try {
      const userData = await authApi.getCurrentUser();
      setCurrentUser(userData);
      localStorage.setItem('user', JSON.stringify({
        id: userData.id,
        email: userData.email,
        name: userData.name
      }));
      return userData;
    } catch (error) {
      // If refresh fails, try token refresh first
      try {
        await authApi.refreshToken();
        const userData = await authApi.getCurrentUser();
        setCurrentUser(userData);
        localStorage.setItem('user', JSON.stringify({
          id: userData.id,
          email: userData.email,
          name: userData.name
        }));
        return userData;
      } catch (refreshError) {
        // Both attempts failed, user is likely logged out
        setCurrentUser(null);
        localStorage.removeItem('user');
        throw refreshError;
      }
    }
  };

  const value = {
    currentUser,
    loading,
    login,
    register,
    googleLogin,
    logout,
    updateProfile,
    refreshUser
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};