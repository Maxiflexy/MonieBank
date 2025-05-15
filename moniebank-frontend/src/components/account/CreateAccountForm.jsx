import React, { useState, useEffect } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import accountApi from '../../api/accountApi';
import authApi from '../../api/authApi';
import { useAuth } from '../../hooks/useAuth';

const CreateAccountForm = () => {
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const [submitting, setSubmitting] = useState(false);
  
  const accountTypes = [
    { value: 'SAVINGS', label: 'Savings' },
    { value: 'CHECKING', label: 'Checking' },
    { value: 'BUSINESS', label: 'Business' }
  ];
  
  const validationSchema = Yup.object({
    fullName: Yup.string()
      .required('Full name is required')
      .max(50, 'Full name must be at most 50 characters'),
    accountType: Yup.string()
      .required('Account type is required')
      .oneOf(accountTypes.map(type => type.value), 'Invalid account type'),
    email: Yup.string()
      .email('Invalid email format')
      .required('Email is required')
  });
  
  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      // Create account with the correct DTO format
      const accountData = {
        fullName: values.fullName,
        email: values.email,
        accountType: values.accountType
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
        initialValues={{ 
          fullName: currentUser?.name || '', 
          email: currentUser?.email || '', 
          accountType: 'SAVINGS' 
        }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        {({ isSubmitting: formikSubmitting }) => (
          <Form className="create-account-form">
            <div className="form-group">
              <label htmlFor="fullName">Full Name</label>
              <Field 
                type="text" 
                id="fullName" 
                name="fullName" 
                placeholder="Enter full name" 
              />
              <ErrorMessage name="fullName" component="div" className="error-message" />
            </div>
            
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <Field 
                type="email" 
                id="email" 
                name="email" 
                placeholder="Enter email" 
              />
              <ErrorMessage name="email" component="div" className="error-message" />
            </div>
            
            <div className="form-group">
              <label htmlFor="accountType">Account Type</label>
              <Field as="select" id="accountType" name="accountType">
                {accountTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </Field>
              <ErrorMessage name="accountType" component="div" className="error-message" />
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
    </div>
  );
};

export default CreateAccountForm;