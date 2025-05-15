import React, { useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Footer from '../components/common/Footer';
import '../styles/landingPage.css';

const LandingPage = () => {
  const location = useLocation();

  // Handle hash links for smooth scrolling
  useEffect(() => {
    if (location.hash) {
      const id = location.hash.substring(1); // remove the # symbol
      const element = document.getElementById(id);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }, [location]);

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
        
        {/* About Section */}
        <section id="about" className="about-section">
          <div className="container">
            <h2>About MonieBank</h2>
            <div className="about-content">
              <div className="about-text">
                <p>MonieBank is a modern digital banking platform designed to make financial management easy and accessible for everyone. Our mission is to provide secure, fast, and user-friendly banking services that meet the needs of today's digital world.</p>
                <p>Founded with a vision to transform the banking experience, we leverage cutting-edge technology to offer innovative solutions while maintaining the highest standards of security and reliability.</p>
              </div>
              <div className="about-image">
                <div className="image-placeholder">
                  <img src="/src/assets/about-banking.svg" alt="About MonieBank" onError={(e) => e.target.style.display = 'none'} />
                </div>
              </div>
            </div>
          </div>
        </section>
        
        {/* Features Section */}
        <section id="features" className="features">
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
        
        {/* Contact Section */}
        <section id="contact" className="contact-section">
          <div className="container">
            <h2>Contact Us</h2>
            <div className="contact-content">
              <div className="contact-info">
                <div className="contact-item">
                  <div className="contact-icon">📧</div>
                  <div className="contact-detail">
                    <h3>Email</h3>
                    <p><a href="mailto:support@moniebank.com">support@moniebank.com</a></p>
                  </div>
                </div>
                <div className="contact-item">
                  <div className="contact-icon">📞</div>
                  <div className="contact-detail">
                    <h3>Phone</h3>
                    <p><a href="tel:+2348187626932">+(234) 81-8762-6932</a></p>
                  </div>
                </div>
                <div className="contact-item">
                  <div className="contact-icon">📍</div>
                  <div className="contact-detail">
                    <h3>Address</h3>
                    <p>Lagos, Nigeria</p>
                  </div>
                </div>
              </div>
              <div className="contact-form">
                <form>
                  <div className="form-group">
                    <label htmlFor="name">Name</label>
                    <input type="text" id="name" placeholder="Your name" />
                  </div>
                  <div className="form-group">
                    <label htmlFor="email">Email</label>
                    <input type="email" id="email" placeholder="Your email" />
                  </div>
                  <div className="form-group">
                    <label htmlFor="message">Message</label>
                    <textarea id="message" rows="5" placeholder="How can we help?"></textarea>
                  </div>
                  <button type="submit" className="btn btn-primary">Send Message</button>
                </form>
              </div>
            </div>
          </div>
        </section>
      </main>
      
      <Footer />
    </div>
  );
};

export default LandingPage;