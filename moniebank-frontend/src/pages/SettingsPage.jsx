import React, { useState } from 'react';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import Footer from '../components/common/Footer';
import { toast } from 'react-toastify';

const SettingsPage = () => {
  const [darkMode, setDarkMode] = useState(false);
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [sessionTimeout, setSessionTimeout] = useState(30);
  
  const handleSaveGeneral = () => {
    toast.success('General settings saved successfully!');
  };
  
  const handleSaveNotifications = () => {
    toast.success('Notification settings saved successfully!');
  };
  
  const handleSaveSecurity = () => {
    toast.success('Security settings saved successfully!');
  };
  
  return (
    <div className="settings-page">
      <Navbar />
      
      <div className="dashboard-container">
        <Sidebar />
        
        <main className="dashboard-content">
          <div className="page-header">
            <h1>Settings</h1>
          </div>
          
          <div className="settings-grid">
            <div className="settings-card">
              <h2>General Settings</h2>
              
              <div className="settings-section">
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Dark Mode</h3>
                    <p>Switch between light and dark theme</p>
                  </div>
                  <div className="setting-control">
                    <label className="toggle-switch">
                      <input 
                        type="checkbox"
                        checked={darkMode}
                        onChange={() => setDarkMode(!darkMode)}
                      />
                      <span className="toggle-slider"></span>
                    </label>
                  </div>
                </div>
                
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Language</h3>
                    <p>Choose your preferred language</p>
                  </div>
                  <div className="setting-control">
                    <select className="select-control">
                      <option value="en">English</option>
                      <option value="es">Spanish</option>
                      <option value="fr">French</option>
                      <option value="de">German</option>
                    </select>
                  </div>
                </div>
              </div>
              
              <div className="setting-actions">
                <button 
                  className="btn btn-primary"
                  onClick={handleSaveGeneral}
                >
                  Save Changes
                </button>
              </div>
            </div>
            
            <div className="settings-card">
              <h2>Notification Settings</h2>
              
              <div className="settings-section">
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Email Notifications</h3>
                    <p>Receive notifications via email</p>
                  </div>
                  <div className="setting-control">
                    <label className="toggle-switch">
                      <input 
                        type="checkbox"
                        checked={notificationsEnabled}
                        onChange={() => setNotificationsEnabled(!notificationsEnabled)}
                      />
                      <span className="toggle-slider"></span>
                    </label>
                  </div>
                </div>
                
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Transaction Alerts</h3>
                    <p>Get notified about new transactions</p>
                  </div>
                  <div className="setting-control">
                    <label className="toggle-switch">
                      <input 
                        type="checkbox"
                        checked={true}
                      />
                      <span className="toggle-slider"></span>
                    </label>
                  </div>
                </div>
                
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Balance Alerts</h3>
                    <p>Get notified about low balance</p>
                  </div>
                  <div className="setting-control">
                    <label className="toggle-switch">
                      <input 
                        type="checkbox"
                        checked={true}
                      />
                      <span className="toggle-slider"></span>
                    </label>
                  </div>
                </div>
              </div>
              
              <div className="setting-actions">
                <button 
                  className="btn btn-primary"
                  onClick={handleSaveNotifications}
                >
                  Save Changes
                </button>
              </div>
            </div>
            
            <div className="settings-card">
              <h2>Security Settings</h2>
              
              <div className="settings-section">
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Change Password</h3>
                    <p>Update your account password</p>
                  </div>
                  <div className="setting-control">
                    <button className="btn btn-secondary">
                      Change Password
                    </button>
                  </div>
                </div>
                
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Two-Factor Authentication</h3>
                    <p>Add an extra layer of security</p>
                  </div>
                  <div className="setting-control">
                    <button className="btn btn-secondary">
                      Enable 2FA
                    </button>
                  </div>
                </div>
                
                <div className="setting-item">
                  <div className="setting-info">
                    <h3>Session Timeout (minutes)</h3>
                    <p>Automatically log out after inactivity</p>
                  </div>
                  <div className="setting-control">
                    <input 
                      type="number"
                      className="number-input"
                      min="5"
                      max="60"
                      value={sessionTimeout}
                      onChange={(e) => setSessionTimeout(parseInt(e.target.value))}
                    />
                  </div>
                </div>
              </div>
              
              <div className="setting-actions">
                <button 
                  className="btn btn-primary"
                  onClick={handleSaveSecurity}
                >
                  Save Changes
                </button>
              </div>
            </div>
          </div>
        </main>
      </div>
      
      <Footer />
      
      <style jsx>{`
        .settings-page {
          min-height: 100vh;
          display: flex;
          flex-direction: column;
          background-color: var(--background-color);
        }
        
        .page-header {
          margin-bottom: 30px;
        }
        
        .page-header h1 {
          margin: 0;
          color: var(--secondary-color);
        }
        
        .settings-grid {
          display: grid;
          grid-template-columns: 1fr;
          gap: 30px;
        }
        
        .settings-card {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 30px;
        }
        
        .settings-card h2 {
          color: var(--secondary-color);
          margin-top: 0;
          margin-bottom: 20px;
          padding-bottom: 15px;
          border-bottom: 1px solid var(--border-color);
        }
        
        .settings-section {
          margin-bottom: 30px;
        }
        
        .setting-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px 0;
          border-bottom: 1px solid var(--border-color);
        }
        
        .setting-item:last-child {
          border-bottom: none;
        }
        
        .setting-info h3 {
          margin: 0 0 5px 0;
          font-size: 1.1rem;
        }
        
        .setting-info p {
          margin: 0;
          color: var(--light-text-color);
          font-size: 0.9rem;
        }
        
        .setting-control {
          min-width: 100px;
          display: flex;
          justify-content: flex-end;
        }
        
        .toggle-switch {
          position: relative;
          display: inline-block;
          width: 60px;
          height: 30px;
        }
        
        .toggle-switch input {
          opacity: 0;
          width: 0;
          height: 0;
        }
        
        .toggle-slider {
          position: absolute;
          cursor: pointer;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background-color: #ccc;
          transition: .4s;
          border-radius: 34px;
        }
        
        .toggle-slider:before {
          position: absolute;
          content: "";
          height: 22px;
          width: 22px;
          left: 4px;
          bottom: 4px;
          background-color: white;
          transition: .4s;
          border-radius: 50%;
        }
        
        input:checked + .toggle-slider {
          background-color: var(--primary-color);
        }
        
        input:checked + .toggle-slider:before {
          transform: translateX(30px);
        }
        
        .select-control {
          padding: 8px;
          border-radius: var(--border-radius);
          border: 1px solid var(--border-color);
          min-width: 150px;
        }
        
        .number-input {
          width: 80px;
          padding: 8px;
          border-radius: var(--border-radius);
          border: 1px solid var(--border-color);
          text-align: center;
        }
        
        .setting-actions {
          display: flex;
          justify-content: flex-end;
        }
        
        @media (min-width: 992px) {
          .settings-grid {
            grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
          }
        }
        
        @media (max-width: 768px) {
          .setting-item {
            flex-direction: column;
            align-items: flex-start;
          }
          
          .setting-control {
            margin-top: 10px;
            width: 100%;
            justify-content: flex-start;
          }
        }
      `}</style>
    </div>
  );
};

export default SettingsPage;