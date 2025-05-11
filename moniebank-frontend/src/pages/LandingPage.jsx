import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';
import '../styles/landingPage.css';

const LandingPage = () => {
  return (
    <div className="landing-page">
      <Navbar />
      
      <main>
        <section className="hero">
          <div className="hero-content">
            <h1>Welcome to MonieBank</h1>
            <p>Your modern banking solution for a digital world</p>
            <div className="cta-buttons">
              <Link to="/register" className="btn btn-primary">
                Get Started
              </Link>
              <Link to="/login" className="btn btn-secondary">
                Login
              </Link>
            </div>
          </div>
        </section>
        
        <section className="features">
          <h2>Why Choose MonieBank?</h2>
          <div className="feature-cards">
            <div className="feature-card">
              <h3>Secure Banking</h3>
              <p>State-of-the-art security features to keep your money safe</p>
            </div>
            <div className="feature-card">
              <h3>Easy Transfers</h3>
              <p>Send money instantly to anyone, anywhere</p>
            </div>
            <div className="feature-card">
              <h3>Track Spending</h3>
              <p>Monitor your transactions and manage your finances effectively</p>
            </div>
          </div>
        </section>
        
        <section className="how-it-works">
          <h2>How It Works</h2>
          <div className="steps">
            <div className="step">
              <h3>1. Create an Account</h3>
              <p>Sign up in minutes with your email or Google account</p>
            </div>
            <div className="step">
              <h3>2. Add Your Details</h3>
              <p>Verify your identity and set up your banking profile</p>
            </div>
            <div className="step">
              <h3>3. Start Banking</h3>
              <p>Deposit, withdraw, and transfer money with ease</p>
            </div>
          </div>
        </section>
      </main>
      
      <Footer />
    </div>
  );
};

export default LandingPage;