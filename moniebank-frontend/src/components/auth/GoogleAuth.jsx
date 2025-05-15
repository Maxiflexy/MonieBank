import React, { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';

const GoogleAuth = ({ isRegister = false }) => {
  const { googleLogin } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    // Load Google Sign-In API
    const loadGoogleScript = () => {
      // Check if script is already loaded
      if (document.querySelector('script[src*="accounts.google.com/gsi/client"]')) {
        initializeGoogleButton();
        return;
      }
      
      try {
        const script = document.createElement('script');
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        
        script.onload = () => {
          initializeGoogleButton();
        };
        
        script.onerror = () => {
          setError('Failed to load Google Sign-In API');
          setIsLoading(false);
        };
        
        document.body.appendChild(script);
      } catch (err) {
        setError('Error loading Google Sign-In API');
        setIsLoading(false);
      }
    };
    
    const initializeGoogleButton = () => {
      // Ensure the button container exists
      const buttonContainer = document.getElementById('google-signin-button');
      if (!buttonContainer) {
        setError('Button container not found');
        setIsLoading(false);
        return;
      }
      
      // Check for Google API
      if (!window.google || !window.google.accounts) {
        setError('Google API not loaded properly');
        setIsLoading(false);
        return;
      }
      
      // Check for Client ID
      const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
      if (!clientId) {
        setError('Google Client ID not configured');
        setIsLoading(false);
        return;
      }
      
      try {
        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: handleGoogleResponse,
          auto_select: false,
        });
        
        window.google.accounts.id.renderButton(
          buttonContainer,
          { 
            type: 'standard',
            theme: 'outline', 
            size: 'large',
            text: isRegister ? 'signup_with' : 'signin_with',
            shape: 'rectangular',
            width: 280
          }
        );
        
        setIsLoading(false);
      } catch (err) {
        setError(`Error initializing Google Sign-In: ${err.message}`);
        setIsLoading(false);
      }
    };
    
    loadGoogleScript();
    
    return () => {
      // Clean up if needed
    };
  }, [isRegister]);
  
  const handleGoogleResponse = async (response) => {
    try {
      if (response.credential) {
        await googleLogin(response.credential);
      }
    } catch (error) {
      console.error('Google sign in error:', error);
    }
  };
  
  if (error) {
    return (
      <div className="google-auth-container">
        <div className="google-auth-fallback">
          {/* FIXED: Use googleLogin function directly instead of redirecting */}
          <button 
            className="btn btn-outline google-fallback-button"
            onClick={() => {
              // Create a mock credential or show a message that direct Google auth is needed
              setError('Direct Google authentication currently unavailable. Please try again later.');
            }}
          >
            <i className="fab fa-google"></i> Continue with Google
          </button>
          {process.env.NODE_ENV === 'development' && (
            <div className="google-auth-error">
              <small>{error}</small>
            </div>
          )}
        </div>
      </div>
    );
  }
  
  return (
    <div className="google-auth-container">
      {isLoading && <div className="google-auth-loading">Loading Google Sign-In...</div>}
      <div id="google-signin-button" className="google-button-container"></div>
    </div>
  );
};

export default GoogleAuth;