import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import '../../styles/navbar.css';
import logo from '../../assets/logo.png';

const Navbar = () => {
  const { currentUser, logout } = useAuth();
  const location = useLocation();
  
  const scrollToSection = (sectionId) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  // Check if we're on the landing page
  const isLandingPage = location.pathname === '/';
  
  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <Link to="/" className="navbar-logo">
            <img src={logo} alt="MonieBank Logo" className="brand-logo" />
            <span className="brand-text">MonieBank</span>
          </Link>
        </div>
        
        <div className="navbar-menu">
          <ul className="navbar-nav">
            {currentUser ? (
              <>
                <li className="nav-item">
                  <Link to="/dashboard" className="nav-link">
                    Dashboard
                  </Link>
                </li>
                <li className="nav-item">
                  <Link to="/accounts" className="nav-link">
                    Accounts
                  </Link>
                </li>
                <li className="nav-item">
                  <Link to="/transactions" className="nav-link">
                    Transactions
                  </Link>
                </li>
              </>
            ) : (
              <>
                <li className="nav-item">
                  {isLandingPage ? (
                    <a href="#about" className="nav-link" onClick={(e) => {
                      e.preventDefault();
                      scrollToSection('about');
                    }}>
                      About
                    </a>
                  ) : (
                    <Link to="/#about" className="nav-link">
                      About
                    </Link>
                  )}
                </li>
                <li className="nav-item">
                  {isLandingPage ? (
                    <a href="#features" className="nav-link" onClick={(e) => {
                      e.preventDefault();
                      scrollToSection('features');
                    }}>
                      Features
                    </a>
                  ) : (
                    <Link to="/#features" className="nav-link">
                      Features
                    </Link>
                  )}
                </li>
                <li className="nav-item">
                  {isLandingPage ? (
                    <a href="#contact" className="nav-link" onClick={(e) => {
                      e.preventDefault();
                      scrollToSection('contact');
                    }}>
                      Contact
                    </a>
                  ) : (
                    <Link to="/#contact" className="nav-link">
                      Contact
                    </Link>
                  )}
                </li>
              </>
            )}
          </ul>
          
          <div className="navbar-auth">
            {currentUser ? (
              <>
                <div className="dropdown">
                  <button className="dropdown-toggle">
                    {currentUser.name || 'Account'} ▼
                  </button>
                  <div className="dropdown-menu">
                    <Link to="/profile" className="dropdown-item">
                      Profile
                    </Link>
                    <Link to="/settings" className="dropdown-item">
                      Settings
                    </Link>
                    <hr />
                    <button 
                      className="dropdown-item logout-button"
                      onClick={logout}
                    >
                      Logout
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-secondary">
                  Login
                </Link>
                <Link to="/register" className="btn btn-primary">
                  Sign Up
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;