import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';
import LoginForm from '../components/auth/LoginForm';
import GoogleAuth from '../components/auth/GoogleAuth';
import '../styles/authPages.css';

const LoginPage = () => {
  return (
    <div className="auth-page">
      <Navbar />
      
      <main className="auth-container">
        <div className="auth-card">
          <h1>Login to MonieBank</h1>
          
          <LoginForm />
          
          <div className="auth-separator">
            <span>OR</span>
          </div>
          
          <GoogleAuth />
          
          <div className="auth-links">
            <p>
              Don't have an account? <Link to="/register">Sign up</Link>
            </p>
          </div>
        </div>
      </main>
      
      <Footer />
    </div>
  );
};

export default LoginPage;