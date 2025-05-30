import React, { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import '../../styles/sidebar.css';

const Sidebar = () => {
  const { currentUser, logout } = useAuth();
  const [isVisible, setIsVisible] = useState(true);

  // Check screen size and set sidebar visibility initially
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth < 992) {
        setIsVisible(false);
      } else {
        setIsVisible(true);
      }
    };

    // Set initial state
    handleResize();
    
    // Add resize listener
    window.addEventListener('resize', handleResize);
    
    // Clean up
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const toggleSidebar = () => {
    setIsVisible(!isVisible);
  };

  return (
    <>
      <aside className={`sidebar ${isVisible ? 'sidebar-visible' : 'sidebar-hidden'}`}>
        <div className="sidebar-user">
          <div className="user-avatar">
            {currentUser?.name?.charAt(0) || 'U'}
          </div>
          <div className="user-info">
            <h3 className="user-name">{currentUser?.name || 'User'}</h3>
            <p className="user-email">{currentUser?.email || 'No email'}</p>
          </div>
        </div>
        
        <nav className="sidebar-nav">
          <ul className="nav-list">
            <li className="nav-item">
              <NavLink to="/dashboard" className="nav-link">
                <i className="fas fa-home"></i>
                Dashboard
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/accounts" className="nav-link">
                <i className="fas fa-wallet"></i>
                Accounts
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/transactions" className="nav-link">
                <i className="fas fa-exchange-alt"></i>
                Transactions
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/deposit" className="nav-link">
                <i className="fas fa-arrow-down"></i>
                Deposit
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/withdraw" className="nav-link">
                <i className="fas fa-arrow-up"></i>
                Withdraw
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/transfer" className="nav-link">
                <i className="fas fa-paper-plane"></i>
                Transfer
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/profile" className="nav-link">
                <i className="fas fa-user"></i>
                Profile
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/settings" className="nav-link">
                <i className="fas fa-cog"></i>
                Settings
              </NavLink>
            </li>
          </ul>
        </nav>
        
        <div className="sidebar-footer">
          <button className="logout-button" onClick={logout}>
            <i className="fas fa-sign-out-alt"></i>
            Logout
          </button>
        </div>
      </aside>
      
      {/* Sidebar toggle button */}
      <button 
        className="sidebar-toggle" 
        onClick={toggleSidebar} 
        aria-label={isVisible ? "Hide sidebar" : "Show sidebar"}
      >
        <i className={`fas ${isVisible ? 'fa-chevron-left' : 'fa-chevron-right'}`}></i>
      </button>
    </>
  );
};

export default Sidebar;