// moniebank-frontend/src/components/common/ProtectedRoute.jsx
import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

const ProtectedRoute = ({ children }) => {
  const { currentUser, loading, refreshUser } = useAuth();
  const [authChecking, setAuthChecking] = useState(false);
  const [shouldRedirect, setShouldRedirect] = useState(false);

  useEffect(() => {
    // If we don't have a current user but we're not loading,
    // try to refresh user data in case we have valid cookies
    const checkAuth = async () => {
      if (!currentUser && !loading && !authChecking) {
        setAuthChecking(true);
        try {
          console.log('ProtectedRoute: Attempting to refresh user...');
          await refreshUser();
          console.log('ProtectedRoute: User refresh successful');
        } catch (error) {
          console.error('ProtectedRoute: Auth check failed:', error);
          // Only set redirect after we've actually tried to refresh
          setShouldRedirect(true);
        } finally {
          setAuthChecking(false);
        }
      } else if (!currentUser && !loading && !authChecking) {
        // No user and not loading/checking - redirect
        setShouldRedirect(true);
      }
    };

    checkAuth();
  }, [currentUser, loading, refreshUser, authChecking]);

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
        <p>Authenticating...</p>
        <style>{`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  // Only redirect if we've actually determined the user is not authenticated
  if (shouldRedirect || (!currentUser && !loading && !authChecking)) {
    return <Navigate to="/login" />;
  }
  
  return children;
};

export default ProtectedRoute;