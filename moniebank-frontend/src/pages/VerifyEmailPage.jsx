import React, { useEffect, useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';
import authApi from '../api/authApi';
import '../styles/verifyEmail.css';

const VerifyEmailPage = () => {
  const [verificationStatus, setVerificationStatus] = useState('verifying');
  const [message, setMessage] = useState('');
  const location = useLocation();
  
  useEffect(() => {
    const verifyEmail = async () => {
      const query = new URLSearchParams(location.search);
      const token = query.get('token');
      
      if (!token) {
        setVerificationStatus('failed');
        setMessage('Invalid verification link. No token provided.');
        return;
      }
      
      try {
        const response = await authApi.verifyEmail(token);
        setVerificationStatus('success');
        setMessage(response.message || 'Email verified successfully. You can now login.');
      } catch (error) {
        setVerificationStatus('failed');
        setMessage(
          error.response?.data?.message || 
          'Email verification failed. The token may be invalid or expired.'
        );
      }
    };
    
    verifyEmail();
  }, [location.search]);
  
  return (
    <div className="verify-email-page">
      <Navbar />
      
      <main className="verify-email-container">
        <div className="verify-email-card">
          <h1>Email Verification</h1>
          
          {verificationStatus === 'verifying' && (
            <div className="verifying">
              <div className="spinner"></div>
              <p>Verifying your email...</p>
            </div>
          )}
          
          {verificationStatus === 'success' && (
            <div className="success">
              <div className="success-icon">✓</div>
              <p>{message}</p>
              <Link to="/login" className="btn btn-primary">
                Go to Login
              </Link>
            </div>
          )}
          
          {verificationStatus === 'failed' && (
            <div className="failed">
              <div className="failed-icon">✗</div>
              <p>{message}</p>
              <p>
                If you need a new verification link, please 
                <Link to="/login"> click here to login</Link> and request a new verification email.
              </p>
            </div>
          )}
        </div>
      </main>
      
      <Footer />
    </div>
  );
};

export default VerifyEmailPage;