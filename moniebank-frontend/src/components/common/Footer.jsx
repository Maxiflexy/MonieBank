import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import '../../styles/footer.css';

const Footer = () => {
  const currentYear = new Date().getFullYear();
  const location = useLocation();
  
  // Check if we're on the landing page
  const isLandingPage = location.pathname === '/';
  
  const scrollToSection = (sectionId) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };
  
  return (
    <footer className="footer">
      <div className="footer-container">
        <div className="footer-main">
          <div className="footer-column">
            <h3 className="footer-title">MonieBank</h3>
            <p className="footer-description">
              Your modern banking solution for a digital world. Secure, fast, and easy to use.
            </p>
          </div>
          
          <div className="footer-column">
            <h3 className="footer-title">Links</h3>
            <ul className="footer-links">
              <li>
                <Link to="/">Home</Link>
              </li>
              <li>
                {isLandingPage ? (
                  <a href="#about" onClick={(e) => {
                    e.preventDefault();
                    scrollToSection('about');
                  }}>About</a>
                ) : (
                  <Link to="/#about">About</Link>
                )}
              </li>
              <li>
                {isLandingPage ? (
                  <a href="#features" onClick={(e) => {
                    e.preventDefault();
                    scrollToSection('features');
                  }}>Features</a>
                ) : (
                  <Link to="/#features">Features</Link>
                )}
              </li>
              <li>
                {isLandingPage ? (
                  <a href="#contact" onClick={(e) => {
                    e.preventDefault();
                    scrollToSection('contact');
                  }}>Contact</a>
                ) : (
                  <Link to="/#contact">Contact</Link>
                )}
              </li>
            </ul>
          </div>
          
          <div className="footer-column">
            <h3 className="footer-title">Legal</h3>
            <ul className="footer-links">
              <li>
                <Link to="/">Terms of Service</Link>
              </li>
              <li>
                <Link to="/">Privacy Policy</Link>
              </li>
              <li>
                <Link to="/">Security</Link>
              </li>
            </ul>
          </div>
          
          <div className="footer-column">
            <h3 className="footer-title">Contact</h3>
            <ul className="footer-contact">
              <li>
                <span className="contact-label">Email:</span>
                <a href="mailto:support@moniebank.com">support@moniebank.com</a>
              </li>
              <li>
                <span className="contact-label">Phone:</span>
                <a href="tel:+2348187626932">+(234) 81-8762-6932</a>
              </li>
              <li>
                <span className="contact-label">Address:</span>
                <p>Lagos, Nigeria</p>
              </li>
            </ul>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p className="copyright">
            &copy; {currentYear} MonieBank. All rights reserved.
          </p>
          <div className="social-links">
            <a href="#" className="social-link">
              <i className="fab fa-facebook-f"></i>
            </a>
            <a href="#" className="social-link">
              <i className="fab fa-twitter"></i>
            </a>
            <a href="#" className="social-link">
              <i className="fab fa-instagram"></i>
            </a>
            <a href="#" className="social-link">
              <i className="fab fa-linkedin-in"></i>
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;