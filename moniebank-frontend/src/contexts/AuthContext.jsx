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
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const userData = await authApi.getCurrentUser();
          setCurrentUser(userData);
        } catch (error) {
          console.error('Failed to load user:', error);
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      }
      setLoading(false);
    };

    loadUser();
  }, []);

  const login = async (credentials) => {
    try {
      const response = await authApi.login(credentials);
      localStorage.setItem('token', response.accessToken);
      localStorage.setItem('user', JSON.stringify({
        id: response.userId,
        email: response.email,
        name: response.name
      }));
      
      setCurrentUser({
        id: response.userId,
        email: response.email,
        name: response.name
      });
      
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
      localStorage.setItem('token', response.accessToken);
      localStorage.setItem('user', JSON.stringify({
        id: response.userId,
        email: response.email,
        name: response.name
      }));
      
      setCurrentUser({
        id: response.userId,
        email: response.email,
        name: response.name
      });
      
      toast.success('Login with Google successful!');
      navigate('/dashboard');
      return response;
    } catch (error) {
      toast.error('Login with Google failed');
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setCurrentUser(null);
    navigate('/login');
    toast.info('Logged out successfully');
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

  const value = {
    currentUser,
    loading,
    login,
    register,
    googleLogin,
    logout,
    updateProfile
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};