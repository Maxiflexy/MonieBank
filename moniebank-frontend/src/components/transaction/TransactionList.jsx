import React from 'react';
import PropTypes from 'prop-types';

const TransactionList = ({ transactions }) => {
  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  // Format date
  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  // Get appropriate icon based on transaction type
  const getTransactionIcon = (type) => {
    switch(type?.toUpperCase()) {
      case 'DEPOSIT':
        return '⬇️';
      case 'WITHDRAWAL':
        return '⬆️';
      case 'TRANSFER_OUT':
        return '↗️';
      case 'TRANSFER_IN':
        return '↘️';
      case 'PAYMENT':
        return '💵';
      case 'FEE':
        return '💸';
      case 'INTEREST':
        return '💰';
      default:
        return '🔄';
    }
  };

  // Get appropriate color based on transaction type
  const getAmountClass = (type) => {
    // Convert type to uppercase for consistent comparison
    const upperType = type?.toUpperCase();
    
    if (upperType === 'DEPOSIT' || upperType === 'INTEREST' || upperType === 'TRANSFER_IN') {
      return 'amount-positive';
    } else if (upperType === 'WITHDRAWAL' || upperType === 'FEE' || upperType === 'TRANSFER_OUT' || upperType === 'PAYMENT') {
      return 'amount-negative';
    }
    
    // Default case
    return '';
  };
  
  // Get prefix based on transaction type
  const getAmountPrefix = (type) => {
    const upperType = type?.toUpperCase();
    
    if (upperType === 'DEPOSIT' || upperType === 'INTEREST' || upperType === 'TRANSFER_IN') {
      return '+ ';
    } else if (upperType === 'WITHDRAWAL' || upperType === 'FEE' || upperType === 'TRANSFER_OUT' || upperType === 'PAYMENT') {
      return '- ';
    }
    
    return '';
  };

  return (
    <div className="transaction-list">
      {transactions.map((transaction) => (
        <div key={transaction.id} className="transaction-item">
          <div className="transaction-icon">
            {getTransactionIcon(transaction.type)}
          </div>
          <div className="transaction-info">
            <h4 className="transaction-title">{transaction.description || transaction.type}</h4>
            <p className="transaction-date">{formatDate(transaction.transactionDate || transaction.date || new Date())}</p>
          </div>
          <div className={`transaction-amount ${getAmountClass(transaction.type)}`}>
            {getAmountPrefix(transaction.type)}
            {formatCurrency(Math.abs(transaction.amount || 0))}
          </div>
        </div>
      ))}
      <style jsx>{`
        .transaction-list {
          border-radius: var(--border-radius);
          overflow: hidden;
        }
        
        .transaction-item {
          display: flex;
          align-items: center;
          padding: 15px;
          border-bottom: 1px solid var(--border-color);
          background-color: white;
          transition: background-color 0.2s ease;
        }
        
        .transaction-item:last-child {
          border-bottom: none;
        }
        
        .transaction-item:hover {
          background-color: rgba(0, 0, 0, 0.02);
        }
        
        .transaction-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background-color: rgba(52, 152, 219, 0.1);
          display: flex;
          justify-content: center;
          align-items: center;
          margin-right: 15px;
          font-size: 1.2rem;
        }
        
        .transaction-info {
          flex: 1;
        }
        
        .transaction-title {
          margin: 0 0 5px 0;
          font-size: 1rem;
          font-weight: 500;
        }
        
        .transaction-date {
          margin: 0;
          font-size: 0.85rem;
          color: var(--light-text-color);
        }
        
        .transaction-amount {
          font-weight: 600;
        }
        
        .amount-positive {
          color: var(--success-color);
        }
        
        .amount-negative {
          color: var(--error-color);
        }
      `}</style>
    </div>
  );
};

TransactionList.propTypes = {
  transactions: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string,
      description: PropTypes.string,
      type: PropTypes.string,
      amount: PropTypes.number,
      date: PropTypes.string,
      transactionDate: PropTypes.string
    })
  ).isRequired
};

export default TransactionList;