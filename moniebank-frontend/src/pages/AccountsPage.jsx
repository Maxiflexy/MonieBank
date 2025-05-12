import React, { useState, useEffect } from 'react';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import Footer from '../components/common/Footer';
import AccountsList from '../components/account/AccountsList';
import CreateAccountForm from '../components/account/CreateAccountForm';
import accountApi from '../api/accountApi';
import '../styles/accounts.css';

const AccountsPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState('list'); // 'list' or 'create'
  
  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const accountsData = await accountApi.getAllAccounts();
        setAccounts(accountsData);
      } catch (error) {
        console.error('Error fetching accounts:', error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchAccounts();
  }, []);
  
  return (
    <div className="accounts-page">
      <Navbar />
      
      <div className="dashboard-container">
        <Sidebar />
        
        <main className="dashboard-content">
          <div className="page-header">
            <h1>{view === 'list' ? 'Your Accounts' : 'Create New Account'}</h1>
            {view === 'list' ? (
              <button 
                className="btn btn-primary" 
                onClick={() => setView('create')}
              >
                + New Account
              </button>
            ) : (
              <button 
                className="btn btn-secondary" 
                onClick={() => setView('list')}
              >
                Back to Accounts
              </button>
            )}
          </div>
          
          {view === 'list' ? (
            <div className="accounts-container">
              <AccountsList accounts={accounts} loading={loading} />
            </div>
          ) : (
            <div className="create-account-container">
              <CreateAccountForm />
            </div>
          )}
        </main>
      </div>
      
      <Footer />
      
      <style jsx>{`
        .accounts-page {
          min-height: 100vh;
          display: flex;
          flex-direction: column;
          background-color: var(--background-color);
        }
        
        .page-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 30px;
        }
        
        .page-header h1 {
          margin: 0;
          color: var(--secondary-color);
        }
        
        .accounts-container, 
        .create-account-container {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 20px;
        }
        
        @media (max-width: 768px) {
          .page-header {
            flex-direction: column;
            gap: 15px;
            align-items: flex-start;
          }
        }
      `}</style>
    </div>
  );
};

export default AccountsPage;