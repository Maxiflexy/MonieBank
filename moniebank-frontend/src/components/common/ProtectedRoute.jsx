import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import authApi from '../../api/authApi';

const ProtectedRoute = ({ children }) => {
  const { currentUser, loading, refreshUser } = useAuth();
  const [authChecking, setAuthChecking] = useState(false);

  useEffect(() => {
    // If we don't have a current user but we're not loading,
    // try to refresh user data in case we have valid cookies
    const checkAuth = async () => {
      if (!currentUser && !loading) {
        setAuthChecking(true);
        try {
          await refreshUser();
        } catch (error) {
          console.error('Auth check failed:', error);
        } finally {
          setAuthChecking(false);
        }
      }
    };

    checkAuth();
  }, [currentUser, loading, refreshUser]);

  // Show loading while initial auth check or refresh is happening
  if (loading || authChecking) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        flexDirection: 'column'
      }}>
        <div style={{
          border: '4px solid rgba(0, 0, 0, 0.1)',
          borderRadius: '50%',
          borderTop: '4px solid #3498db',
          width: '40px',
          height: '40px',
          animation: 'spin 1s linear infinite',
          marginBottom: '20px'
        }}></div>
        <p>Loading...</p>
        <style>{`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }
  
  if (!currentUser) {
    return <Navigate to="/login" />;
  }
  
  return children;
};

export default ProtectedRoute;