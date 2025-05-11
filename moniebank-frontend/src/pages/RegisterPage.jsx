import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';
import RegisterForm from '../components/auth/RegisterForm';
import GoogleAuth from '../components/auth/GoogleAuth';
import '../styles/authPages.css';

const RegisterPage = () => {
  return (
    <div className="auth-page">
      <Navbar />
      
      <main className="auth-container">
        <div className="auth-card">
          <h1>Create an Account</h1>
          
          <RegisterForm />
          
          <div className="auth-separator">
            <span>OR</span>
          </div>
          
          <GoogleAuth isRegister />
          
          <div className="auth-links">
            <p>
              Already have an account? <Link to="/login">Login</Link>
            </p>
          </div>
        </div>
      </main>
      
      <Footer />
    </div>
  );
};

export default RegisterPage;