import React from 'react';
import PropTypes from 'prop-types';
import AccountCard from './AccountCard';
import { Link } from 'react-router-dom';

const AccountsList = ({ accounts, loading }) => {
  if (loading) {
    return (
      <div className="accounts-list-loading">
        <div className="spinner"></div>
        <p>Loading accounts...</p>
      </div>
    );
  }

  if (!accounts || accounts.length === 0) {
    return (
      <div className="no-accounts">
        <p>You don't have any accounts yet.</p>
        <Link to="/accounts/create" className="btn btn-primary">
          Create an Account
        </Link>
      </div>
    );
  }

  return (
    <div className="accounts-list">
      <div className="accounts-grid">
        {accounts.map(account => (
          <AccountCard key={account.id} account={account} />
        ))}
        <div className="create-account-card">
          <Link to="/accounts/create" className="create-account-link">
            <div className="create-icon">+</div>
            <h3>Create New Account</h3>
          </Link>
        </div>
      </div>
      <style jsx>{`
        .accounts-list {
          width: 100%;
        }
        
        .accounts-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 20px;
        }
        
        .accounts-list-loading {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          min-height: 200px;
        }
        
        .spinner {
          border: 4px solid rgba(0, 0, 0, 0.1);
          border-radius: 50%;
          border-top: 4px solid var(--primary-color);
          width: 40px;
          height: 40px;
          animation: spin 1s linear infinite;
          margin-bottom: 20px;
        }
        
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        
        .no-accounts {
          text-align: center;
          padding: 40px;
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
        }
        
        .no-accounts p {
          margin-bottom: 20px;
          color: var(--light-text-color);
        }
        
        .create-account-card {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 20px;
          height: 100%;
          display: flex;
          flex-direction: column;
          border: 2px dashed var(--border-color);
          transition: transform 0.3s ease, border-color 0.3s ease;
        }
        
        .create-account-card:hover {
          transform: translateY(-5px);
          border-color: var(--primary-color);
        }
        
        .create-account-link {
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          text-align: center;
          height: 100%;
          color: var(--light-text-color);
        }
        
        .create-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          background-color: rgba(52, 152, 219, 0.1);
          color: var(--primary-color);
          display: flex;
          justify-content: center;
          align-items: center;
          font-size: 24px;
          margin-bottom: 15px;
        }
        
        .create-account-link h3 {
          font-weight: 500;
        }
      `}</style>
    </div>
  );
};

AccountsList.propTypes = {
  accounts: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string,
      type: PropTypes.string,
      balance: PropTypes.number
    })
  ),
  loading: PropTypes.bool
};

AccountsList.defaultProps = {
  loading: false
};

export default AccountsList;