import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import transactionApi from '../../api/transactionApi';

const WithdrawForm = ({ accounts, onComplete }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const validationSchema = Yup.object({
    accountId: Yup.string()
      .required('Please select an account'),
    amount: Yup.number()
      .required('Amount is required')
      .positive('Amount must be positive')
      .min(1, 'Minimum withdrawal amount is $1')
      .test(
        'is-less-than-balance',
        'Insufficient funds',
        function(value) {
          if (!value) return true;
          
          const { accountId } = this.parent;
          const account = accounts.find(acc => acc.id === accountId);
          
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
      const withdrawData = {
        accountId: values.accountId,
        amount: parseFloat(values.amount),
        description: values.description || 'Withdrawal'
      };
      
      await transactionApi.withdraw(withdrawData);
      toast.success('Withdrawal successful!');
      resetForm();
      if (onComplete) {
        onComplete();
      }
    } catch (error) {
      console.error('Error processing withdrawal:', error);
      let errorMessage = 'Failed to process withdrawal';
      
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
  
  return (
    <div className="transaction-form withdraw-form">
      <Formik
        initialValues={{ accountId: accounts.length > 0 ? accounts[0].id : '', amount: '', description: '' }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
        enableReinitialize
      >
        {({ isSubmitting: formikSubmitting, values }) => {
          const selectedAccount = accounts.find(acc => acc.id === values.accountId);
          const availableBalance = selectedAccount ? selectedAccount.balance : 0;
          
          return (
            <Form>
              <div className="form-group">
                <label htmlFor="accountId">Select Account</label>
                <Field as="select" name="accountId" id="accountId">
                  <option value="">Select an account</option>
                  {accounts.map(account => (
                    <option key={account.id} value={account.id}>
                      {account.name} ({account.type}) - {formatCurrency(account.balance)}
                    </option>
                  ))}
                </Field>
                <ErrorMessage name="accountId" component="div" className="error-message" />
              </div>
              
              {selectedAccount && (
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
                  placeholder="Enter withdrawal amount"
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
                  placeholder="Enter a description for this withdrawal"
                />
                <ErrorMessage name="description" component="div" className="error-message" />
              </div>
              
              <div className="withdraw-info">
                <div className="info-icon">⚠️</div>
                <p>
                  Withdrawals are subject to daily limits and available funds in your account.
                  Once confirmed, withdrawals cannot be cancelled.
                </p>
              </div>
              
              <button
                type="submit"
                className="btn btn-primary btn-block"
                disabled={isSubmitting || formikSubmitting || !selectedAccount || availableBalance <= 0}
              >
                {isSubmitting || formikSubmitting ? 'Processing...' : 'Complete Withdrawal'}
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
        
        .form-group {
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
        
        .withdraw-info {
          display: flex;
          align-items: flex-start;
          background-color: rgba(243, 156, 18, 0.1);
          border-radius: var(--border-radius);
          padding: 15px;
          margin-bottom: 20px;
        }
        
        .info-icon {
          margin-right: 10px;
          font-size: 1.2rem;
        }
        
        .withdraw-info p {
          margin: 0;
          font-size: 0.9rem;
          color: var(--secondary-color);
        }
        
        .btn-block {
          width: 100%;
          padding: 12px;
          font-size: 1rem;
        }
      `}</style>
    </div>
  );
};

WithdrawForm.propTypes = {
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

export default WithdrawForm;