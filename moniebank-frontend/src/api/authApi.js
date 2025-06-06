import axiosInstance from "../utils/axiosConfig";

const authApi = {
  register: async (userData) => {
    const response = await axiosInstance.post("/auth/signup", userData);
    return response.data;
  },

  login: async (credentials) => {
    const response = await axiosInstance.post("/auth/login", credentials);
    return response.data;
  },

  logout: async () => {
    const response = await axiosInstance.post("/auth/logout");
    return response.data;
  },

  refreshToken: async () => {
    const response = await axiosInstance.post("/auth/refresh");
    return response.data;
  },

  verifyEmail: async (token) => {
    try {
      // Simplified approach - just send the token as a query parameter
      const response = await axiosInstance.get(`/auth/verify-email`, {
        params: { token },
      });
      return response.data;
    } catch (error) {
      console.error("Email verification error:", error);
      throw error;
    }
  },

  resendVerification: async (email) => {
    const response = await axiosInstance.post(
      `/auth/resend-verification?email=${email}`
    );
    return response.data;
  },

  googleLogin: async (tokenId) => {
    const response = await axiosInstance.post("/auth/oauth2/google", {
      tokenId,
    });
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await axiosInstance.get("/auth/user/me");
    return response.data;
  },

  updateProfile: async (profileData) => {
    const response = await axiosInstance.put("/auth/user/update", profileData);
    return response.data;
  },

  // Method to check if user is authenticated (useful for route guards)
  checkAuth: async () => {
    try {
      const response = await axiosInstance.get("/auth/user/me");
      return { isAuthenticated: true, user: response.data };
    } catch (error) {
      return { isAuthenticated: false, user: null };
    }
  },
};

export default authApi;