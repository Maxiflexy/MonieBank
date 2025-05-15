// In moniebank-frontend/src/components/transaction/TransferForm.jsx

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import transactionApi from '../../api/transactionApi';

const TransferForm = ({ accounts, onComplete }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [targetAccount, setTargetAccount] = useState(null);
  
  const validationSchema = Yup.object({
    fromAccountId: Yup.string()
      .required('Please select source account'),
    toAccountNumber: Yup.string()
      .required('Please enter destination account number'),
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
        toAccountNumber: values.toAccountNumber,
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
  
  if (accounts.length < 1) {
    return (
      <div className="not-enough-accounts">
        <p>You need at least one account to make a transfer.</p>
        <a href="/accounts/create" className="btn btn-primary">Create Another Account</a>
      </div>
    );
  }
  
  return (
    <div className="transaction-form transfer-form">
      <Formik
        initialValues={{ 
          fromAccountId: accounts.length > 0 ? accounts[0].id : '', 
          toAccountNumber: '',
          amount: '', 
          description: '' 
        }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
        enableReinitialize
      >
        {({ isSubmitting: formikSubmitting, values }) => {
          const sourceAccount = accounts.find(acc => acc.id === values.fromAccountId);
          const availableBalance = sourceAccount ? sourceAccount.balance : 0;
          
          return (
            <Form>
              <div className="form-group">
                <label htmlFor="fromAccountId">From Account</label>
                <Field as="select" name="fromAccountId" id="fromAccountId">
                  <option value="">Select source account</option>
                  {accounts.map(account => (
                    <option key={account.id} value={account.id}>
                      {account.fullName || account.name} ({account.accountType}) - {formatCurrency(account.balance)}
                    </option>
                  ))}
                </Field>
                <ErrorMessage name="fromAccountId" component="div" className="error-message" />
              </div>
              
              <div className="form-group">
                <label htmlFor="toAccountNumber">To Account Number</label>
                <Field 
                  type="text" 
                  name="toAccountNumber" 
                  id="toAccountNumber"
                  placeholder="Enter destination account number"
                />
                <ErrorMessage name="toAccountNumber" component="div" className="error-message" />
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
                    <span>{sourceAccount ? `${sourceAccount.fullName || sourceAccount.name} (${sourceAccount.accountType})` : 'Not selected'}</span>
                  </div>
                  <div className="summary-row">
                    <span>To Account Number:</span>
                    <span>{values.toAccountNumber || 'Not entered'}</span>
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
                  Please verify the recipient's account number before proceeding.
                  Transfers cannot be cancelled once confirmed.
                </p>
              </div>
              
              <button
                type="submit"
                className="btn btn-primary btn-block"
                disabled={
                  isSubmitting || 
                  formikSubmitting || 
                  !sourceAccount || 
                  availableBalance <= 0
                }
              >
                {isSubmitting || formikSubmitting ? 'Processing...' : 'Complete Transfer'}
              </button>
            </Form>
          );
        }}
      </Formik>
    </div>
  );
};

TransferForm.propTypes = {
  accounts: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string,
      fullName: PropTypes.string,
      accountType: PropTypes.string.isRequired,
      balance: PropTypes.number.isRequired,
      accountNumber: PropTypes.string
    })
  ).isRequired,
  onComplete: PropTypes.func
};

export default TransferForm;