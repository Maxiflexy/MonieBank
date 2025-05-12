import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { toast } from 'react-toastify';
import authApi from '../../api/authApi';

const EmailVerification = ({ email, onResendSuccess }) => {
  const [isResending, setIsResending] = useState(false);
  
  const handleResendVerification = async () => {
    if (!email) {
      toast.error('No email address provided.');
      return;
    }
    
    setIsResending(true);
    try {
      const response = await authApi.resendVerification(email);
      toast.success(response.message || 'Verification email resent. Please check your inbox.');
      
      if (onResendSuccess) {
        onResendSuccess();
      }
    } catch (error) {
      console.error('Error resending verification email:', error);
      let errorMessage = 'Failed to resend verification email';
      
      if (error.response && error.response.data) {
        errorMessage = error.response.data.message || errorMessage;
      }
      
      toast.error(errorMessage);
    } finally {
      setIsResending(false);
    }
  };
  
  return (
    <div className="email-verification">
      <div className="verification-icon">📧</div>
      <h2>Verify Your Email</h2>
      <p>
        A verification email has been sent to <strong>{email}</strong>.
        Please check your inbox and click the verification link to activate your account.
      </p>
      <p className="verification-note">
        If you don't see the email, please check your spam folder.
      </p>
      
      <button 
        className="btn btn-secondary" 
        onClick={handleResendVerification}
        disabled={isResending}
      >
        {isResending ? 'Sending...' : 'Resend Verification Email'}
      </button>
      
      <style jsx>{`
        .email-verification {
          text-align: center;
          max-width: 600px;
          margin: 0 auto;
          padding: 30px;
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
        }
        
        .verification-icon {
          font-size: 3rem;
          margin-bottom: 20px;
        }
        
        h2 {
          margin-bottom: 20px;
          color: var(--secondary-color);
        }
        
        p {
          margin-bottom: 15px;
          color: var(--text-color);
        }
        
        .verification-note {
          font-size: 0.9rem;
          color: var(--light-text-color);
          margin-bottom: 25px;
        }
        
        .btn {
          padding: 10px 20px;
        }
      `}</style>
    </div>
  );
};

EmailVerification.propTypes = {
  email: PropTypes.string.isRequired,
  onResendSuccess: PropTypes.func
};

export default EmailVerification;