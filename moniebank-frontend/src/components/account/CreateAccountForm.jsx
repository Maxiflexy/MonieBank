import React, { useState } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import accountApi from '../../api/accountApi';

const CreateAccountForm = () => {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  
  const accountTypes = [
    { value: 'CHECKING', label: 'Checking' },
    { value: 'SAVINGS', label: 'Savings' },
    { value: 'INVESTMENT', label: 'Investment' }
  ];
  
  const validationSchema = Yup.object({
    name: Yup.string()
      .required('Account name is required')
      .max(50, 'Account name must be at most 50 characters'),
    type: Yup.string()
      .required('Account type is required')
      .oneOf(accountTypes.map(type => type.value), 'Invalid account type'),
    initialDeposit: Yup.number()
      .required('Initial deposit is required')
      .min(0, 'Initial deposit must be a positive number')
  });
  
  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      const accountData = {
        name: values.name,
        type: values.type,
        initialDeposit: parseFloat(values.initialDeposit)
      };
      
      const response = await accountApi.createAccount(accountData);
      toast.success('Account created successfully!');
      navigate('/accounts');
      return response;
    } catch (error) {
      console.error('Error creating account:', error);
      let errorMessage = 'Failed to create account';
      
      if (error.response && error.response.data) {
        errorMessage = error.response.data.message || errorMessage;
      }
      
      toast.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };
  
  return (
    <div className="create-account-form-container">
      <Formik
        initialValues={{ name: '', type: '', initialDeposit: '' }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        {({ isSubmitting: formikSubmitting }) => (
          <Form className="create-account-form">
            <div className="form-group">
              <label htmlFor="name">Account Name</label>
              <Field 
                type="text" 
                id="name" 
                name="name" 
                placeholder="Enter account name" 
              />
              <ErrorMessage name="name" component="div" className="error-message" />
            </div>
            
            <div className="form-group">
              <label htmlFor="type">Account Type</label>
              <Field as="select" id="type" name="type">
                <option value="">Select account type</option>
                {accountTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </Field>
              <ErrorMessage name="type" component="div" className="error-message" />
            </div>
            
            <div className="form-group">
              <label htmlFor="initialDeposit">Initial Deposit ($)</label>
              <Field 
                type="number" 
                id="initialDeposit" 
                name="initialDeposit" 
                placeholder="Enter initial deposit amount" 
                min="0"
                step="0.01"
              />
              <ErrorMessage name="initialDeposit" component="div" className="error-message" />
            </div>
            
            <div className="form-actions">
              <button 
                type="button" 
                className="btn btn-secondary" 
                onClick={() => navigate('/accounts')}
                disabled={submitting || formikSubmitting}
              >
                Cancel
              </button>
              <button 
                type="submit" 
                className="btn btn-primary" 
                disabled={submitting || formikSubmitting}
              >
                {submitting || formikSubmitting ? 'Creating...' : 'Create Account'}
              </button>
            </div>
          </Form>
        )}
      </Formik>
      <style jsx>{`
        .create-account-form-container {
          max-width: 600px;
          margin: 0 auto;
        }
        
        .create-account-form {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 30px;
        }
        
        .form-group {
          margin-bottom: 20px;
        }
        
        label {
          display: block;
          margin-bottom: 5px;
          font-weight: 500;
        }
        
        input, select {
          width: 100%;
          padding: 10px;
          border: 1px solid var(--border-color);
          border-radius: var(--border-radius);
          font-size: 1rem;
        }
        
        input:focus, select:focus {
          outline: none;
          border-color: var(--primary-color);
        }
        
        .error-message {
          color: var(--error-color);
          font-size: 0.85rem;
          margin-top: 5px;
        }
        
        .form-actions {
          display: flex;
          justify-content: flex-end;
          gap: 15px;
          margin-top: 30px;
        }
      `}</style>
    </div>
  );
};

export default CreateAccountForm;