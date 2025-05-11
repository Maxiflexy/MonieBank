import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import Footer from '../components/common/Footer';
import AccountCard from '../components/account/AccountCard';
import TransactionList from '../components/transaction/TransactionList';
import accountApi from '../api/accountApi';
import transactionApi from '../api/transactionApi';
import { useAuth } from '../hooks/useAuth';
import '../styles/dashboard.css';

const Dashboard = () => {
  const { currentUser } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // Fetch accounts
        const accountsData = await accountApi.getAllAccounts();
        setAccounts(accountsData);
        
        // If there's at least one account, fetch recent transactions
        if (accountsData.length > 0) {
          const accountId = accountsData[0].id;
          const transactionsData = await transactionApi.getTransactionHistory(accountId);
          setRecentTransactions(transactionsData.content || []);
        }
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchDashboardData();
  }, []);
  
  if (loading) {
    return (
      <div className="dashboard">
        <Navbar />
        <div className="dashboard-container">
          <Sidebar />
          <main className="dashboard-content">
            <div className="loading">Loading dashboard data...</div>
          </main>
        </div>
        <Footer />
      </div>
    );
  }
  
  return (
    <div className="dashboard">
      <Navbar />
      
      <div className="dashboard-container">
        <Sidebar />
        
        <main className="dashboard-content">
          <h1>Welcome, {currentUser?.name || 'User'}!</h1>
          
          <section className="accounts-overview">
            <div className="section-header">
              <h2>Your Accounts</h2>
              <Link to="/accounts" className="view-all">
                View All
              </Link>
            </div>
            
            <div className="accounts-grid">
              {accounts.length > 0 ? (
                accounts.map(account => (
                  <AccountCard key={account.id} account={account} />
                ))
              ) : (
                <div className="no-accounts">
                  <p>You don't have any accounts yet.</p>
                  <Link to="/accounts" className="btn btn-primary">
                    Create an Account
                  </Link>
                </div>
              )}
            </div>
          </section>
          
          <section className="recent-transactions">
            <div className="section-header">
              <h2>Recent Transactions</h2>
              <Link to="/transactions" className="view-all">
                View All
              </Link>
            </div>
            
            {accounts.length > 0 ? (
              recentTransactions.length > 0 ? (
                <TransactionList transactions={recentTransactions.slice(0, 5)} />
              ) : (
                <div className="no-transactions">
                  <p>No recent transactions.</p>
                </div>
              )
            ) : (
              <div className="no-transactions">
                <p>Create an account to start making transactions.</p>
              </div>
            )}
          </section>
          
          <section className="quick-actions">
            <h2>Quick Actions</h2>
            <div className="actions-grid">
              <Link to="/transactions/deposit" className="action-card">
                <div className="action-icon">💰</div>
                <h3>Deposit</h3>
              </Link>
              <Link to="/transactions/withdraw" className="action-card">
                <div className="action-icon">💸</div>
                <h3>Withdraw</h3>
              </Link>
              <Link to="/transactions/transfer" className="action-card">
                <div className="action-icon">↗️</div>
                <h3>Transfer</h3>
              </Link>
              <Link to="/accounts/create" className="action-card">
                <div className="action-icon">➕</div>
                <h3>New Account</h3>
              </Link>
            </div>
          </section>
        </main>
      </div>
      
      <Footer />
    </div>
  );
};

export default Dashboard;