import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';

// Pages
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VerifyEmailPage from './pages/VerifyEmailPage';
import Dashboard from './pages/Dashboard';
import AccountsPage from './pages/AccountsPage';
import AccountDetailsPage from './pages/AccountDetailsPage'; // Add this import
import TransactionsPage from './pages/TransactionsPage';
import DepositPage from './pages/DepositPage'; // Add these pages
import WithdrawPage from './pages/WithdrawPage';
import TransferPage from './pages/TransferPage';
import ProfilePage from './pages/ProfilePage';
import SettingsPage from './pages/SettingsPage';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route 
        path="/dashboard" 
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/accounts" 
        element={
          <ProtectedRoute>
            <AccountsPage />
          </ProtectedRoute>
        } 
      />
      {/* Add route for account details */}
      <Route 
        path="/accounts/:id" 
        element={
          <ProtectedRoute>
            <AccountDetailsPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/transactions" 
        element={
          <ProtectedRoute>
            <TransactionsPage />
          </ProtectedRoute>
        } 
      />
      {/* Add routes for deposit, withdraw, transfer pages */}
      <Route 
        path="/deposit" 
        element={
          <ProtectedRoute>
            <DepositPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/withdraw" 
        element={
          <ProtectedRoute>
            <WithdrawPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/transfer" 
        element={
          <ProtectedRoute>
            <TransferPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/profile" 
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/settings" 
        element={
          <ProtectedRoute>
            <SettingsPage />
          </ProtectedRoute>
        } 
      />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
        <ToastContainer position="top-right" autoClose={5000} />
      </AuthProvider>
    </Router>
  );
}

export default App;