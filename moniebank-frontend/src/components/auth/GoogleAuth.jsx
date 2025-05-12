import React, { useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';

const GoogleAuth = ({ isRegister = false }) => {
  const { googleLogin } = useAuth();
  
  useEffect(() => {
    // Load Google Sign-In API
    const loadGoogleScript = () => {
      // Check if script is already loaded
      if (document.querySelector('script[src*="accounts.google.com/gsi/client"]')) {
        return;
      }
      
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      document.body.appendChild(script);
      
      script.onload = () => {
        initializeGoogleButton();
      };
    };
    
    const initializeGoogleButton = () => {
      if (window.google && window.google.accounts) {
        window.google.accounts.id.initialize({
          client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
          callback: handleGoogleResponse,
          auto_select: false,
        });
        
        window.google.accounts.id.renderButton(
          document.getElementById('google-signin-button'),
          { 
            type: 'standard',
            theme: 'outline', 
            size: 'large',
            text: isRegister ? 'signup_with' : 'signin_with',
            shape: 'rectangular',
            width: 280
          }
        );
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
  
  return (
    <div className="google-auth-container">
      <div id="google-signin-button" className="google-button-container"></div>
    </div>
  );
};

export default GoogleAuth;