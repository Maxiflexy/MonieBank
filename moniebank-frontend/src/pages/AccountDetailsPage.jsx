import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import Footer from '../components/common/Footer';
import TransactionList from '../components/transaction/TransactionList';
import accountApi from '../api/accountApi';
import transactionApi from '../api/transactionApi';
import { toast } from 'react-toastify';

const AccountDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchAccountDetails = async () => {
      try {
        // Fetch account details
        const accountData = await accountApi.getAccountById(id);
        setAccount(accountData);
        
        // Fetch transactions for this account
        const transactionsData = await transactionApi.getTransactionHistory(id);
        setTransactions(transactionsData.content || []);
      } catch (error) {
        console.error('Error fetching account details:', error);
        toast.error('Failed to load account details');
        // Navigate back to accounts page if account not found
        navigate('/accounts');
      } finally {
        setLoading(false);
      }
    };
    
    fetchAccountDetails();
  }, [id, navigate]);
  
  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };
  
  if (loading) {
    return (
      <div className="account-details-page">
        <Navbar />
        <div className="dashboard-container">
          <Sidebar />
          <main className="dashboard-content">
            <div className="loading">
              <div className="spinner"></div>
              <p>Loading account details...</p>
            </div>
          </main>
        </div>
        <Footer />
      </div>
    );
  }
  
  return (
    <div className="account-details-page">
      <Navbar />
      
      <div className="dashboard-container">
        <Sidebar />
        
        <main className="dashboard-content">
          <div className="page-header">
            <h1>Account Details</h1>
            <button 
              className="btn btn-secondary" 
              onClick={() => navigate('/accounts')}
            >
              Back to Accounts
            </button>
          </div>
          
          {account ? (
            <>
              <div className="account-details-card">
                <div className="account-header">
                  <div className="account-type-badge">
                    {account.accountType || account.type}
                  </div>
                  <h2>{account.fullName || account.name || 'Account'}</h2>
                </div>
                
                <div className="account-info-grid">
                  <div className="info-item">
                    <label>Account Number</label>
                    <div className="info-value">{account.accountNumber}</div>
                  </div>
                  
                  <div className="info-item">
                    <label>Current Balance</label>
                    <div className="info-value balance">
                      {formatCurrency(account.balance || 0)}
                    </div>
                  </div>
                  
                  <div className="info-item">
                    <label>Account Holder</label>
                    <div className="info-value">{account.fullName || account.name}</div>
                  </div>
                  
                  <div className="info-item">
                    <label>Email</label>
                    <div className="info-value">{account.email}</div>
                  </div>
                  
                  <div className="info-item">
                    <label>Created</label>
                    <div className="info-value">
                      {new Date(account.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                  
                  <div className="info-item">
                    <label>Last Updated</label>
                    <div className="info-value">
                      {new Date(account.updatedAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>
                
                <div className="account-actions">
                  <button 
                    className="btn btn-primary"
                    onClick={() => navigate('/deposit', { state: { preSelectedAccount: account.id } })}
                  >
                    Deposit
                  </button>
                  <button 
                    className="btn btn-primary"
                    onClick={() => navigate('/withdraw', { state: { preSelectedAccount: account.id } })}
                  >
                    Withdraw
                  </button>
                  <button 
                    className="btn btn-primary"
                    onClick={() => navigate('/transfer', { state: { preSelectedAccount: account.id } })}
                  >
                    Transfer
                  </button>
                </div>
              </div>
              
              <div className="account-transactions">
                <h2>Recent Transactions</h2>
                {transactions.length > 0 ? (
                  <TransactionList transactions={transactions} />
                ) : (
                  <div className="no-transactions">
                    <p>No transactions found for this account.</p>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="account-not-found">
              <p>Account not found or you don't have permission to view it.</p>
              <button 
                className="btn btn-primary"
                onClick={() => navigate('/accounts')}
              >
                Back to Accounts
              </button>
            </div>
          )}
        </main>
      </div>
      
      <Footer />
      
      <style jsx>{`
        .account-details-page {
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
        
        .account-details-card {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 30px;
          margin-bottom: 30px;
        }
        
        .account-header {
          display: flex;
          align-items: center;
          margin-bottom: 20px;
        }
        
        .account-type-badge {
          background-color: var(--primary-color);
          color: white;
          padding: 5px 10px;
          border-radius: var(--border-radius);
          font-size: 0.85rem;
          font-weight: 500;
          margin-right: 15px;
        }
        
        .account-header h2 {
          margin: 0;
          color: var(--secondary-color);
        }
        
        .account-info-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
          gap: 20px;
          margin-bottom: 30px;
          border-top: 1px solid var(--border-color);
          padding-top: 20px;
        }
        
        .info-item {
          margin-bottom: 10px;
        }
        
        .info-item label {
          display: block;
          font-size: 0.85rem;
          color: var(--light-text-color);
          margin-bottom: 5px;
        }
        
        .info-value {
          font-weight: 500;
          padding: 8px;
          background-color: var(--background-color);
          border-radius: var(--border-radius);
        }
        
        .info-value.balance {
          font-size: 1.2rem;
          color: var(--primary-color);
          font-weight: 600;
        }
        
        .account-actions {
          display: flex;
          gap: 15px;
          justify-content: flex-end;
          margin-top: 20px;
        }
        
        .account-transactions {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 30px;
        }
        
        .account-transactions h2 {
          margin: 0 0 20px 0;
          color: var(--secondary-color);
          font-size: 1.4rem;
        }
        
        .no-transactions,
        .account-not-found {
          text-align: center;
          padding: 30px;
          color: var(--light-text-color);
        }
        
        .no-transactions p,
        .account-not-found p {
          margin-bottom: 20px;
        }
        
        @media (max-width: 768px) {
          .account-info-grid {
            grid-template-columns: 1fr;
          }
          
          .account-actions {
            flex-direction: column;
          }
          
          .page-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 15px;
          }
        }
      `}</style>
    </div>
  );
};

export default AccountDetailsPage;