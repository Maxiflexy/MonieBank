import axiosInstance from '../utils/axiosConfig';

const transactionApi = {
  getTransactionHistory: async (accountId) => {
    const response = await axiosInstance.get(`/transactions/history/${accountId}`);
    return response.data;
  },
  
  deposit: async (depositData) => {
    const response = await axiosInstance.post('/transactions/deposit', depositData);
    return response.data;
  },
  
  withdraw: async (withdrawData) => {
    const response = await axiosInstance.post('/transactions/withdraw', withdrawData);
    return response.data;
  },
  
  transfer: async (transferData) => {
    const response = await axiosInstance.post('/transactions/transfer', transferData);
    return response.data;
  },
  
  getTransactionsByDateRange: async (startDate, endDate, page = 0, size = 10) => {
    const response = await axiosInstance.get('/transactions/date-range', {
      params: {
        startDate,
        endDate,
        page,
        size
      }
    });
    return response.data;
  }
};

export default transactionApi;