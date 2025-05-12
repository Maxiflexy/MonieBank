import React, { useState, useEffect } from 'react';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import Footer from '../components/common/Footer';
import TransactionList from '../components/transaction/TransactionList';
import DepositForm from '../components/transaction/DepositForm';
import WithdrawForm from '../components/transaction/WithdrawForm';
import TransferForm from '../components/transaction/TransferForm';
import accountApi from '../api/accountApi';
import transactionApi from '../api/transactionApi';
import '../styles/transactions.css';

const TransactionsPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [transactionType, setTransactionType] = useState('history'); // 'history', 'deposit', 'withdraw', or 'transfer'
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const accountsData = await accountApi.getAllAccounts();
        setAccounts(accountsData);
        
        if (accountsData.length > 0) {
          setSelectedAccount(accountsData[0].id);
        }
      } catch (error) {
        console.error('Error fetching accounts:', error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchAccounts();
  }, []);
  
  useEffect(() => {
    const fetchTransactions = async () => {
      if (selectedAccount) {
        try {
          setLoading(true);
          const transactionsData = await transactionApi.getTransactionHistory(selectedAccount);
          setTransactions(transactionsData.content || []);
        } catch (error) {
          console.error('Error fetching transactions:', error);
        } finally {
          setLoading(false);
        }
      }
    };
    
    if (transactionType === 'history') {
      fetchTransactions();
    }
  }, [selectedAccount, transactionType]);
  
  const refreshTransactions = async () => {
    if (selectedAccount) {
      try {
        setLoading(true);
        const transactionsData = await transactionApi.getTransactionHistory(selectedAccount);
        setTransactions(transactionsData.content || []);
      } catch (error) {
        console.error('Error refreshing transactions:', error);
      } finally {
        setLoading(false);
      }
    }
  };
  
  const handleAccountChange = (e) => {
    setSelectedAccount(e.target.value);
  };
  
  const renderContent = () => {
    if (accounts.length === 0) {
      return (
        <div className="no-accounts">
          <p>You don't have any accounts yet. Please create an account first.</p>
          <button
            className="btn btn-primary"
            onClick={() => window.location.href = '/accounts'}
          >
            Create Account
          </button>
        </div>
      );
    }
    
    switch (transactionType) {
      case 'deposit':
        return <DepositForm accounts={accounts} onComplete={() => {
          setTransactionType('history');
          refreshTransactions();
        }} />;
      case 'withdraw':
        return <WithdrawForm accounts={accounts} onComplete={() => {
          setTransactionType('history');
          refreshTransactions();
        }} />;
      case 'transfer':
        return <TransferForm accounts={accounts} onComplete={() => {
          setTransactionType('history');
          refreshTransactions();
        }} />;
      case 'history':
      default:
        return (
          <div className="transaction-history">
            <div className="account-selector">
              <label htmlFor="account-select">Select Account:</label>
              <select
                id="account-select"
                value={selectedAccount || ''}
                onChange={handleAccountChange}
                disabled={loading || accounts.length === 0}
              >
                {accounts.map(account => (
                  <option key={account.id} value={account.id}>
                    {account.name} ({account.type}) - {new Intl.NumberFormat('en-US', {
                      style: 'currency',
                      currency: 'USD'
                    }).format(account.balance)}
                  </option>
                ))}
              </select>
            </div>
            
            {loading ? (
              <div className="loading">
                <div className="spinner"></div>
                <p>Loading transactions...</p>
              </div>
            ) : transactions.length > 0 ? (
              <TransactionList transactions={transactions} />
            ) : (
              <div className="no-transactions">
                <p>No transactions found for this account.</p>
              </div>
            )}
          </div>
        );
    }
  };
  
  return (
    <div className="transactions-page">
      <Navbar />
      
      <div className="dashboard-container">
        <Sidebar />
        
        <main className="dashboard-content">
          <div className="page-header">
            <h1>Transactions</h1>
          </div>
          
          <div className="transaction-tabs">
            <button
              className={`tab-button ${transactionType === 'history' ? 'active' : ''}`}
              onClick={() => setTransactionType('history')}
            >
              Transaction History
            </button>
            <button
              className={`tab-button ${transactionType === 'deposit' ? 'active' : ''}`}
              onClick={() => setTransactionType('deposit')}
              disabled={accounts.length === 0}
            >
              Deposit
            </button>
            <button
              className={`tab-button ${transactionType === 'withdraw' ? 'active' : ''}`}
              onClick={() => setTransactionType('withdraw')}
              disabled={accounts.length === 0}
            >
              Withdraw
            </button>
            <button
              className={`tab-button ${transactionType === 'transfer' ? 'active' : ''}`}
              onClick={() => setTransactionType('transfer')}
              disabled={accounts.length === 0}
            >
              Transfer
            </button>
          </div>
          
          <div className="transaction-content">
            {renderContent()}
          </div>
        </main>
      </div>
      
      <Footer />
      
      <style jsx>{`
        .transactions-page {
          min-height: 100vh;
          display: flex;
          flex-direction: column;
          background-color: var(--background-color);
        }
        
        .page-header {
          margin-bottom: 20px;
        }
        
        .page-header h1 {
          margin: 0;
          color: var(--secondary-color);
        }
        
        .transaction-tabs {
          display: flex;
          margin-bottom: 20px;
          border-bottom: 1px solid var(--border-color);
        }
        
        .tab-button {
          padding: 10px 20px;
          background: none;
          border: none;
          border-bottom: 3px solid transparent;
          font-weight: 500;
          color: var(--light-text-color);
          cursor: pointer;
        }
        
        .tab-button:hover:not(:disabled) {
          color: var(--primary-color);
        }
        
        .tab-button.active {
          color: var(--primary-color);
          border-bottom-color: var(--primary-color);
        }
        
        .tab-button:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
        
        .transaction-content {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 20px;
        }
        
        .account-selector {
          margin-bottom: 20px;
        }
        
        .account-selector label {
          display: block;
          margin-bottom: 5px;
          font-weight: 500;
        }
        
        .account-selector select {
          width: 100%;
          padding: 10px;
          border: 1px solid var(--border-color);
          border-radius: var(--border-radius);
          background-color: white;
        }
        
        .no-accounts,
        .no-transactions {
          text-align: center;
          padding: 30px;
        }
        
        .no-accounts p,
        .no-transactions p {
          margin-bottom: 20px;
          color: var(--light-text-color);
        }
        
        @media (max-width: 768px) {
          .transaction-tabs {
            flex-wrap: wrap;
          }
          
          .tab-button {
            flex: 1 0 calc(50% - 10px);
            text-align: center;
          }
        }
      `}</style>
    </div>
  );
};

export default TransactionsPage;