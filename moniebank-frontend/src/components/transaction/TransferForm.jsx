import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import transactionApi from '../../api/transactionApi';

const TransferForm = ({ accounts, onComplete }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const validationSchema = Yup.object({
    fromAccountId: Yup.string()
      .required('Please select source account'),
    toAccountId: Yup.string()
      .required('Please select destination account')
      .test(
        'not-same-account',
        'Source and destination accounts must be different',
        function(value) {
          const { fromAccountId } = this.parent;
          return value !== fromAccountId;
        }
      ),
    amount: Yup.number()
      .required('Amount is required')
      .positive('Amount must be positive')
      .min(1, 'Minimum transfer amount is $1')
      .test(
        'is-less-than-balance',
        'Insufficient funds',
        function(value) {
          if (!value) return true;
          
          const { fromAccountId } = this.parent;
          const account = accounts.find(acc => acc.id === fromAccountId);
          
          if (!account) return true;
          return value <= account.balance;
        }
      ),
    description: Yup.string()
      .max(100, 'Description must be at most 100 characters')
  });
  
  const handleSubmit = async (values, { resetForm }) => {
    setIsSubmitting(true);
    try {
      const transferData = {
        fromAccountId: values.fromAccountId,
        toAccountId: values.toAccountId,
        amount: parseFloat(values.amount),
        description: values.description || 'Transfer'
      };
      
      await transactionApi.transfer(transferData);
      toast.success('Transfer successful!');
      resetForm();
      if (onComplete) {
        onComplete();
      }
    } catch (error) {
      console.error('Error processing transfer:', error);
      let errorMessage = 'Failed to process transfer';
      
      if (error.response && error.response.data) {
        errorMessage = error.response.data.message || errorMessage;
      }
      
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };
  
  // Format currency for account display
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };
  
  if (accounts.length < 2) {
    return (
      <div className="not-enough-accounts">
        <p>You need at least two accounts to make a transfer.</p>
        <a href="/accounts/create" className="btn btn-primary">Create Another Account</a>
      </div>
    );
  }
  
  return (
    <div className="transaction-form transfer-form">
      <Formik
        initialValues={{ 
          fromAccountId: accounts.length > 0 ? accounts[0].id : '', 
          toAccountId: accounts.length > 1 ? accounts[1].id : '',
          amount: '', 
          description: '' 
        }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
        enableReinitialize
      >
        {({ isSubmitting: formikSubmitting, values }) => {
          const sourceAccount = accounts.find(acc => acc.id === values.fromAccountId);
          const destinationAccount = accounts.find(acc => acc.id === values.toAccountId);
          const availableBalance = sourceAccount ? sourceAccount.balance : 0;
          
          return (
            <Form>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="fromAccountId">From Account</label>
                  <Field as="select" name="fromAccountId" id="fromAccountId">
                    <option value="">Select source account</option>
                    {accounts.map(account => (
                      <option key={account.id} value={account.id}>
                        {account.name} ({account.type}) - {formatCurrency(account.balance)}
                      </option>
                    ))}
                  </Field>
                  <ErrorMessage name="fromAccountId" component="div" className="error-message" />
                </div>
                
                <div className="form-group">
                  <label htmlFor="toAccountId">To Account</label>
                  <Field as="select" name="toAccountId" id="toAccountId">
                    <option value="">Select destination account</option>
                    {accounts.map(account => (
                      <option 
                        key={account.id} 
                        value={account.id}
                        disabled={account.id === values.fromAccountId}
                      >
                        {account.name} ({account.type}) - {formatCurrency(account.balance)}
                      </option>
                    ))}
                  </Field>
                  <ErrorMessage name="toAccountId" component="div" className="error-message" />
                </div>
              </div>
              
              {sourceAccount && (
                <div className="account-balance-info">
                  <p>Available Balance: <strong>{formatCurrency(availableBalance)}</strong></p>
                </div>
              )}
              
              <div className="form-group">
                <label htmlFor="amount">Amount ($)</label>
                <Field
                  type="number"
                  name="amount"
                  id="amount"
                  placeholder="Enter transfer amount"
                  min="1"
                  step="0.01"
                  max={availableBalance}
                />
                <ErrorMessage name="amount" component="div" className="error-message" />
              </div>
              
              <div className="form-group">
                <label htmlFor="description">Description (Optional)</label>
                <Field
                  as="textarea"
                  name="description"
                  id="description"
                  placeholder="Enter a description for this transfer"
                />
                <ErrorMessage name="description" component="div" className="error-message" />
              </div>
              
              <div className="transfer-summary">
                <h3>Transfer Summary</h3>
                <div className="summary-details">
                  <div className="summary-row">
                    <span>From:</span>
                    <span>{sourceAccount ? `${sourceAccount.name} (${sourceAccount.type})` : 'Not selected'}</span>
                  </div>
                  <div className="summary-row">
                    <span>To:</span>
                    <span>{destinationAccount ? `${destinationAccount.name} (${destinationAccount.type})` : 'Not selected'}</span>
                  </div>
                  <div className="summary-row">
                    <span>Amount:</span>
                    <span>{values.amount ? formatCurrency(parseFloat(values.amount)) : '$0.00'}</span>
                  </div>
                </div>
              </div>
              
              <div className="transfer-info">
                <div className="info-icon">ℹ️</div>
                <p>
                  Transfers between your accounts are processed immediately.
                  Please verify all information before confirming.
                </p>
              </div>
              
              <button
                type="submit"
                className="btn btn-primary btn-block"
                disabled={
                  isSubmitting || 
                  formikSubmitting || 
                  !sourceAccount || 
                  !destinationAccount || 
                  availableBalance <= 0 ||
                  values.fromAccountId === values.toAccountId
                }
              >
                {isSubmitting || formikSubmitting ? 'Processing...' : 'Complete Transfer'}
              </button>
            </Form>
          );
        }}
      </Formik>
      
      <style jsx>{`
        .transaction-form {
          max-width: 600px;
          margin: 0 auto;
        }
        
        .form-row {
          display: flex;
          gap: 20px;
          margin-bottom: 20px;
        }
        
        .form-group {
          flex: 1;
          margin-bottom: 20px;
        }
        
        label {
          display: block;
          margin-bottom: 5px;
          font-weight: 500;
        }
        
        input, select, textarea {
          width: 100%;
          padding: 12px;
          border: 1px solid var(--border-color);
          border-radius: var(--border-radius);
          font-size: 1rem;
        }
        
        input:focus, select:focus, textarea:focus {
          outline: none;
          border-color: var(--primary-color);
        }
        
        textarea {
          height: 100px;
          resize: vertical;
        }
        
        .error-message {
          color: var(--error-color);
          font-size: 0.85rem;
          margin-top: 5px;
        }
        
        .account-balance-info {
          background-color: var(--background-color);
          padding: 12px;
          border-radius: var(--border-radius);
          margin-bottom: 20px;
        }
        
        .account-balance-info p {
          margin: 0;
          font-size: 0.9rem;
        }
        
        .transfer-summary {
          background-color: var(--background-color);
          border-radius: var(--border-radius);
          padding: 15px;
          margin-bottom: 20px;
        }
        
        .transfer-summary h3 {
          margin-top: 0;
          margin-bottom: 15px;
          font-size: 1.1rem;
          color: var(--secondary-color);
        }
        
        .summary-row {
          display: flex;
          justify-content: space-between;
          padding: 8px 0;
          border-bottom: 1px solid var(--border-color);
        }
        
        .summary-row:last-child {
          border-bottom: none;
        }
        
        .summary-row span:first-child {
          font-weight: 500;
          color: var(--light-text-color);
        }
        
        .transfer-info {
          display: flex;
          align-items: flex-start;
          background-color: rgba(52, 152, 219, 0.1);
          border-radius: var(--border-radius);
          padding: 15px;
          margin-bottom: 20px;
        }
        
        .info-icon {
          margin-right: 10px;
          font-size: 1.2rem;
        }
        
        .transfer-info p {
          margin: 0;
          font-size: 0.9rem;
          color: var(--secondary-color);
        }
        
        .btn-block {
          width: 100%;
          padding: 12px;
          font-size: 1rem;
        }
        
        .not-enough-accounts {
          text-align: center;
          padding: 30px;
        }
        
        .not-enough-accounts p {
          margin-bottom: 20px;
          color: var(--light-text-color);
        }
        
        @media (max-width: 768px) {
          .form-row {
            flex-direction: column;
            gap: 0;
          }
        }
      `}</style>
    </div>
  );
};

TransferForm.propTypes = {
  accounts: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
      balance: PropTypes.number.isRequired
    })
  ).isRequired,
  onComplete: PropTypes.func
};

export default TransferForm;