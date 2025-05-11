import React, { useState } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useAuth } from '../../hooks/useAuth';

const RegisterForm = () => {
  const { register } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const validationSchema = Yup.object({
    name: Yup.string()
      .required('Full name is required'),
    email: Yup.string()
      .email('Invalid email address')
      .required('Email is required'),
    password: Yup.string()
      .min(6, 'Password must be at least 6 characters')
      .required('Password is required'),
    confirmPassword: Yup.string()
      .oneOf([Yup.ref('password'), null], 'Passwords must match')
      .required('Confirm password is required')
  });
  
  const handleSubmit = async (values) => {
    setIsSubmitting(true);
    try {
      const { confirmPassword, ...userData } = values;
      await register(userData);
      // Registration successful, redirect handled in auth context
    } catch (error) {
      // Error handling is done in auth context
    } finally {
      setIsSubmitting(false);
    }
  };
  
  return (
    <div className="auth-form-container">
      <Formik
        initialValues={{ name: '', email: '', password: '', confirmPassword: '' }}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        {({ isSubmitting: formikSubmitting }) => (
          <Form className="auth-form">
            <div className="form-group">
              <label htmlFor="name">Full Name</label>
              <Field 
                type="text" 
                id="name" 
                name="name" 
                placeholder="Enter your full name" 
              />
              <ErrorMessage name="name" component="div" className="error-message" />
            </div>
            
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
                placeholder="Create a password" 
              />
              <ErrorMessage name="password" component="div" className="error-message" />
            </div>
            
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <Field 
                type="password" 
                id="confirmPassword" 
                name="confirmPassword" 
                placeholder="Confirm your password" 
              />
              <ErrorMessage name="confirmPassword" component="div" className="error-message" />
            </div>
            
            <button 
              type="submit" 
              className="btn btn-primary btn-block" 
              disabled={isSubmitting || formikSubmitting}
            >
              {isSubmitting ? 'Registering...' : 'Register'}
            </button>
          </Form>
        )}
      </Formik>
    </div>
  );
};

export default RegisterForm;