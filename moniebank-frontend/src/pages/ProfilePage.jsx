import React, { useState } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';
import Footer from '../components/common/Footer';
import { useAuth } from '../hooks/useAuth';
import '../styles/profile.css';

const ProfilePage = () => {
  const { currentUser, updateProfile } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const validationSchema = Yup.object({
    name: Yup.string()
      .required('Full name is required'),
    email: Yup.string()
      .email('Invalid email address')
      .required('Email is required'),
    phone: Yup.string()
      .matches(/^[0-9+\-\s()]*$/, 'Invalid phone number format'),
    address: Yup.string()
  });
  
  const handleSubmit = async (values) => {
    setIsSubmitting(true);
    try {
      await updateProfile(values);
      setIsEditing(false);
    } catch (error) {
      console.error('Error updating profile:', error);
      toast.error('Failed to update profile');
    } finally {
      setIsSubmitting(false);
    }
  };
  
  return (
    <div className="profile-page">
      <Navbar />
      
      <div className="dashboard-container">
        <Sidebar />
        
        <main className="dashboard-content">
          <div className="page-header">
            <h1>Your Profile</h1>
            {!isEditing && (
              <button 
                className="btn btn-primary" 
                onClick={() => setIsEditing(true)}
              >
                Edit Profile
              </button>
            )}
          </div>
          
          <div className="profile-card">
            <Formik
              initialValues={{
                name: currentUser?.name || '',
                email: currentUser?.email || '',
                phone: currentUser?.phone || '',
                address: currentUser?.address || ''
              }}
              validationSchema={validationSchema}
              onSubmit={handleSubmit}
              enableReinitialize={true}
            >
              {({ isSubmitting: formikSubmitting, values }) => (
                <Form className="profile-form">
                  <div className="profile-avatar">
                    <div className="avatar-circle">
                      {values.name.charAt(0) || 'U'}
                    </div>
                  </div>
                  
                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="name">Full Name</label>
                      {isEditing ? (
                        <>
                          <Field 
                            type="text" 
                            id="name" 
                            name="name" 
                            placeholder="Enter your full name" 
                          />
                          <ErrorMessage name="name" component="div" className="error-message" />
                        </>
                      ) : (
                        <div className="profile-value">{values.name}</div>
                      )}
                    </div>
                    
                    <div className="form-group">
                      <label htmlFor="email">Email</label>
                      {isEditing ? (
                        <>
                          <Field 
                            type="email" 
                            id="email" 
                            name="email" 
                            placeholder="Enter your email" 
                            disabled
                          />
                          <div className="field-note">Email cannot be changed</div>
                        </>
                      ) : (
                        <div className="profile-value">{values.email}</div>
                      )}
                    </div>
                  </div>
                  
                  <div className="form-row">
                    <div className="form-group">
                      <label htmlFor="phone">Phone Number</label>
                      {isEditing ? (
                        <>
                          <Field 
                            type="text" 
                            id="phone" 
                            name="phone" 
                            placeholder="Enter your phone number" 
                          />
                          <ErrorMessage name="phone" component="div" className="error-message" />
                        </>
                      ) : (
                        <div className="profile-value">{values.phone || 'Not provided'}</div>
                      )}
                    </div>
                    
                    <div className="form-group">
                      <label htmlFor="address">Address</label>
                      {isEditing ? (
                        <>
                          <Field 
                            as="textarea" 
                            id="address" 
                            name="address" 
                            placeholder="Enter your address" 
                          />
                          <ErrorMessage name="address" component="div" className="error-message" />
                        </>
                      ) : (
                        <div className="profile-value">{values.address || 'Not provided'}</div>
                      )}
                    </div>
                  </div>
                  
                  {isEditing && (
                    <div className="form-actions">
                      <button 
                        type="button" 
                        className="btn btn-secondary" 
                        onClick={() => setIsEditing(false)}
                        disabled={isSubmitting || formikSubmitting}
                      >
                        Cancel
                      </button>
                      <button 
                        type="submit" 
                        className="btn btn-primary" 
                        disabled={isSubmitting || formikSubmitting}
                      >
                        {isSubmitting || formikSubmitting ? 'Saving...' : 'Save Changes'}
                      </button>
                    </div>
                  )}
                </Form>
              )}
            </Formik>
            
            <div className="profile-section security-section">
              <h2>Security Settings</h2>
              <div className="security-option">
                <div className="security-info">
                  <h3>Password</h3>
                  <p>Change your account password</p>
                </div>
                <button className="btn btn-secondary">Change Password</button>
              </div>
              
              <div className="security-option">
                <div className="security-info">
                  <h3>Two-Factor Authentication</h3>
                  <p>Add an extra layer of security to your account</p>
                </div>
                <button className="btn btn-secondary">Enable</button>
              </div>
            </div>
          </div>
        </main>
      </div>
      
      <Footer />
      
      <style jsx>{`
        .profile-page {
          min-height: 100vh;
          display: flex;
          flex-direction: column;
          background-color: var(--background-color);
        }
        
        .page-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 30px;
        }
        
        .page-header h1 {
          margin: 0;
          color: var(--secondary-color);
        }
        
        .profile-card {
          background-color: white;
          border-radius: var(--border-radius);
          box-shadow: var(--box-shadow);
          padding: 30px;
        }
        
        .profile-avatar {
          display: flex;
          justify-content: center;
          margin-bottom: 30px;
        }
        
        .avatar-circle {
          width: 100px;
          height: 100px;
          border-radius: 50%;
          background-color: var(--primary-color);
          color: white;
          display: flex;
          justify-content: center;
          align-items: center;
          font-size: 2.5rem;
          font-weight: 600;
        }
        
        .form-row {
          display: flex;
          gap: 20px;
          margin-bottom: 20px;
        }
        
        .form-group {
          flex: 1;
        }
        
        .form-group label {
          display: block;
          margin-bottom: 5px;
          font-weight: 500;
          color: var(--light-text-color);
        }
        
        .form-group input,
        .form-group textarea {
          width: 100%;
          padding: 10px;
          border: 1px solid var(--border-color);
          border-radius: var(--border-radius);
          font-size: 1rem;
        }
        
        .form-group input:focus,
        .form-group textarea:focus {
          outline: none;
          border-color: var(--primary-color);
        }
        
        .form-group textarea {
          min-height: 80px;
          resize: vertical;
        }
        
        .profile-value {
          padding: 10px;
          background-color: var(--background-color);
          border-radius: var(--border-radius);
          font-weight: 500;
        }
        
        .field-note {
          font-size: 0.85rem;
          color: var(--light-text-color);
          margin-top: 5px;
        }
        
        .form-actions {
          display: flex;
          justify-content: flex-end;
          gap: 15px;
          margin-top: 30px;
        }
        
        .profile-section {
          margin-top: 40px;
          padding-top: 30px;
          border-top: 1px solid var(--border-color);
        }
        
        .profile-section h2 {
          font-size: 1.4rem;
          margin-bottom: 20px;
          color: var(--secondary-color);
        }
        
        .security-option {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px 0;
          border-bottom: 1px solid var(--border-color);
        }
        
        .security-option:last-child {
          border-bottom: none;
        }
        
        .security-info h3 {
          margin: 0 0 5px 0;
          font-size: 1.1rem;
        }
        
        .security-info p {
          margin: 0;
          color: var(--light-text-color);
          font-size: 0.9rem;
        }
        
        @media (max-width: 768px) {
          .form-row {
            flex-direction: column;
            gap: 15px;
          }
          
          .page-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 15px;
          }
          
          .security-option {
            flex-direction: column;
            align-items: flex-start;
            gap: 15px;
          }
          
          .security-option button {
            align-self: flex-end;
          }
        }
      `}</style>
    </div>
  );
};

export default ProfilePage;