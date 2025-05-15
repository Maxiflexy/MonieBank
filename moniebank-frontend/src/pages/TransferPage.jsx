import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import Footer from '../components/common/Footer';
import TransferForm from '../components/transaction/TransferForm';
import accountApi from '../api/accountApi';

const TransferPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const location = useLocation();
  const [selectedAccountId, setSelectedAccountId] = useState(null);
  
  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const accountsData = await accountApi.getAllAccounts();
        setAccounts(accountsData);
        
        // Check if an account ID was passed in location state
        if (location.state && location.state.preSelectedAccount) {
          setSelectedAccountId(location.state.preSelectedAccount);
        }
      } catch (error) {
        console.error('Error fetching accounts:', error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchAccounts();
  }, [location.state]);
  
  return (
    <div className="transfer-page">
      <Navbar />
      
      <div className="dashboard-container">
        <Sidebar />
        
        <main className="dashboard-content">
          <div className="page-header">
            <h1>Transfer Funds</h1>
          </div>
          
          <div className="transfer-content">
            {loading ? (
              <div className="loading">
                <div className="spinner"></div>
                <p>Loading accounts...</p>
              </div>
            ) : accounts.length > 0 ? (
              <TransferForm 
                accounts={accounts} 
                preSelectedAccountId={selectedAccountId}
                onComplete={() => {
                  // Refresh accounts after a successful transfer
                  accountApi.getAllAccounts().then(data => setAccounts(data));
                }}
              />
            ) : (
              <div className="no-accounts">
                <p>You need at least one account to make a transfer. Please create an account first.</p>
                <a href="/accounts" className="btn btn-primary">Create Account</a>
              </div>
            )}
          </div>
        </main>
      </div>
      
      <Footer />
      
      <style jsx>{`
        .transfer-page {
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
        
        .transfer-content {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 30px;
        }
        
        .loading, .no-accounts {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 40px 0;
        }
        
        .spinner {
          border: 4px solid rgba(0, 0, 0, 0.1);
          border-radius: 50%;
          border-top: 4px solid var(--primary-color);
          width: 30px;
          height: 30px;
          animation: spin 1s linear infinite;
          margin-bottom: 15px;
        }
        
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        
        .no-accounts p {
          margin-bottom: 20px;
          color: var(--light-text-color);
        }
      `}</style>
    </div>
  );
};

export default TransferPage;