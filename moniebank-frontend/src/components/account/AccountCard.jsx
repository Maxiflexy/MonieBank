import React from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

const AccountCard = ({ account }) => {
  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  // Get appropriate icon based on account type
  const getAccountIcon = (type) => {
    switch(type?.toLowerCase()) {
      case 'savings':
        return '💰';
      case 'checking':
        return '💳';
      case 'investment':
        return '📈';
      case 'credit':
        return '💹';
      default:
        return '🏦';
    }
  };

  return (
    <div className="account-card">
      <div className="account-icon">
        {getAccountIcon(account.type)}
      </div>
      <div className="account-info">
        <h3 className="account-name">{account.name || 'Account'}</h3>
        <p className="account-number">
          {account.accountNumber
            ? `**** ${account.accountNumber.slice(-4)}`
            : 'No account number'}
        </p>
        <p className="account-type">{account.type || 'Standard Account'}</p>
      </div>
      <div className="account-balance">
        <h4 className="balance-label">Balance</h4>
        <p className="balance-amount">{formatCurrency(account.balance || 0)}</p>
      </div>
      <div className="account-actions">
        <Link to={`/accounts/${account.id}`} className="btn btn-secondary btn-sm">
          Details
        </Link>
      </div>
      <style jsx>{`
        .account-card {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 20px;
          display: flex;
          flex-direction: column;
          gap: 15px;
          transition: transform 0.3s ease;
        }
        
        .account-card:hover {
          transform: translateY(-5px);
          box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
        }
        
        .account-icon {
          font-size: 2rem;
          margin-bottom: 10px;
        }
        
        .account-name {
          font-size: 1.1rem;
          margin: 0 0 5px 0;
          color: var(--secondary-color);
        }
        
        .account-number, .account-type {
          margin: 0;
          font-size: 0.85rem;
          color: var(--light-text-color);
        }
        
        .account-balance {
          margin-top: auto;
        }
        
        .balance-label {
          font-size: 0.85rem;
          color: var(--light-text-color);
          margin: 0 0 5px 0;
        }
        
        .balance-amount {
          font-size: 1.4rem;
          font-weight: 600;
          color: var(--secondary-color);
          margin: 0;
        }
        
        .account-actions {
          margin-top: 15px;
          display: flex;
          justify-content: flex-end;
        }
        
        .btn-sm {
          padding: 5px 15px;
          font-size: 0.85rem;
        }
      `}</style>
    </div>
  );
};

AccountCard.propTypes = {
  account: PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    accountNumber: PropTypes.string,
    type: PropTypes.string,
    balance: PropTypes.number
  }).isRequired
};

export default AccountCard;