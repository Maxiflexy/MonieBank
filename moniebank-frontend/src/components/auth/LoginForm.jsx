import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useAuth } from '../../hooks/useAuth';
import authApi from '../../api/authApi';
import { toast } from 'react-toastify';

const LoginForm = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showResendOption, setShowResendOption] = useState(false);
  const [email, setEmail] = useState('');
  
  const validationSchema = Yup.object({
    email: Yup.string()
      .email('Invalid email address')
      .required('Email is required'),
    password: Yup.string()
      .required('Password is required')
  });
  
  const handleSubmit = async (values) => {
    setIsSubmitting(true);
    try {
      await login(values);
      // Login successful, redirect handled in auth context
    } catch (error) {
      if (error.response && error.response.status === 403 && 
          error.response.data.message.includes('not verified')) {
        setShowResendOption(true);
        setEmail(values.email);
      }
    } finally {
      setIsSubmitting(false);
    }
  };
  
  const handleResendVerification = async () => {
    try {
      const response = await authApi.resendVerification(email);
      toast.success(response.message || 'Verification email resent. Please check your inbox.');
    } catch (error) {
      toast.error('Failed to resend verification email. Please try again later.');
    }
  };
  
  return (
    <div className="auth-form-container">
      <Formik
        initialValues={{ email: '', password: '' }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        {({ isSubmitting: formikSubmitting }) => (
          <Form className="auth-form">
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <Field 
                type="email" 
                id="email" 
                name="email" 
                placeholder="Enter your email" 
              />
              <ErrorMessage name="email" component="div" className="error-message" />
            </div>
            
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <Field 
                type="password" 
                id="password" 
                name="password" 
                placeholder="Enter your password" 
              />
              <ErrorMessage name="password" component="div" className="error-message" />
            </div>
            
            <button 
              type="submit" 
              className="btn btn-primary btn-block" 
              disabled={isSubmitting || formikSubmitting}
            >
              {isSubmitting ? 'Logging in...' : 'Login'}
            </button>
            
            {showResendOption && (
              <div className="resend-verification">
                <p>Your email is not verified. Please check your inbox or request a new verification email.</p>
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={handleResendVerification}
                >
                  Resend Verification Email
                </button>
              </div>
            )}
          </Form>
        )}
      </Formik>
    </div>
  );
};

export default LoginForm;