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
          // Ensure cookies are used for authentication
          use_fedcm_for_prompt: false,
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
        setIsLoading(true);
        await googleLogin(response.credential);
        // The AuthContext will handle setting cookies and redirecting
      }
    } catch (error) {
      console.error('Google sign in error:', error);
      setError('Google sign-in failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  if (error) {
    return (
      <div className="google-auth-container">
        <div className="google-auth-fallback">
          <button
            className="btn btn-outline google-fallback-button"
            onClick={() => {
              setError(null);
              setIsLoading(true);
              // Retry initialization
              window.location.reload();
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

      <style jsx>{`
        .google-auth-container {
          display: flex;
          justify-content: center;
          margin: 20px 0;
          min-height: 42px;
        }

        .google-button-container {
          width: 280px;
          height: 42px;
        }

        .google-auth-loading {
          font-size: 0.9rem;
          color: var(--light-text-color);
        }

        .google-auth-fallback {
          width: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
        }

        .google-fallback-button {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 280px;
          padding: 10px;
          background-color: white;
          border: 1px solid var(--border-color);
          border-radius: var(--border-radius);
          gap: 10px;
          transition: background-color 0.3s ease;
          cursor: pointer;
          font-weight: 500;
        }

        .google-fallback-button i {
          color: #4285f4;
          font-size: 1.2rem;
        }

        .google-fallback-button:hover {
          background-color: #f1f1f1;
        }

        .google-auth-error {
          margin-top: 10px;
          color: var(--error-color);
          font-size: 0.8rem;
        }
      `}</style>
    </div>
  );
};

export default GoogleAuth;