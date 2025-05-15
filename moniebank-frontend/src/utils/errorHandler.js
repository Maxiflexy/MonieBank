import { toast } from 'react-toastify';

const ErrorHandler = {
  // Handle API errors with specific messages based on error type
  handleApiError: (error, defaultMessage = 'An error occurred') => {
    let errorMessage = defaultMessage;
    
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      if (error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.response.status === 400) {
        errorMessage = 'Invalid request. Please check your data.';
      } else if (error.response.status === 401) {
        errorMessage = 'Authentication failed. Please login again.';
      } else if (error.response.status === 403) {
        errorMessage = 'You do not have permission to perform this action.';
      } else if (error.response.status === 404) {
        errorMessage = 'The requested resource was not found.';
      } else if (error.response.status === 500) {
        errorMessage = 'Server error. Please try again later.';
      }
    } else if (error.request) {
      // The request was made but no response was received
      errorMessage = 'No response from server. Please check your connection.';
    }
    
    // Display error toast
    toast.error(errorMessage);
    
    // Also return the error message for potential further use
    return errorMessage;
  },
  
  // Handle validation errors from form submissions
  handleValidationError: (errors) => {
    // Display first validation error or generic message
    const firstError = Object.values(errors)[0];
    toast.error(firstError || 'Validation failed. Please check your inputs.');
  },
  
  // Handle transaction-specific errors with more context
  handleTransactionError: (error, transactionType) => {
    let errorMessage;
    
    if (error.response && error.response.data) {
      if (error.response.status === 400 && error.response.data.message.includes('insufficient')) {
        errorMessage = 'Insufficient funds for this transaction.';
      } else if (error.response.status === 404 && transactionType === 'transfer') {
        errorMessage = 'Destination account not found. Please check the account number.';
      } else {
        errorMessage = error.response.data.message || `Failed to process ${transactionType}`;
      }
    } else {
      errorMessage = `Unable to complete ${transactionType}. Please try again later.`;
    }
    
    toast.error(errorMessage);
    return errorMessage;
  }
};

export default ErrorHandler;