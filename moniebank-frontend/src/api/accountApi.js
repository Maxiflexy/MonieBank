import axiosInstance from '../utils/axiosConfig';

const accountApi = {
  getAllAccounts: async () => {
    const response = await axiosInstance.get('/accounts');
    return response.data;
  },
  
  getAccountById: async (accountId) => {
    const response = await axiosInstance.get(`/accounts/${accountId}`);
    return response.data;
  },
  
  getAccountByNumber: async (accountNumber) => {
    const response = await axiosInstance.get(`/accounts/number/${accountNumber}`);
    return response.data;
  },
  
  createAccount: async (accountData) => {
    const response = await axiosInstance.post('/accounts', accountData);
    return response.data;
  }
};

export default accountApi;