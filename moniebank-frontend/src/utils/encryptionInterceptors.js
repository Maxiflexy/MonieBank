// File: moniebank-frontend/src/utils/encryptionInterceptors.js

import cryptoUtils, { ENCRYPTION_FIELDS } from './cryptoUtils';

// Define which endpoints should use encryption
const ENCRYPTED_ENDPOINTS = {
  // Auth Service Endpoints
  '/api/auth/login': { requestFields: ENCRYPTION_FIELDS.LOGIN, responseFields: ENCRYPTION_FIELDS.USER_RESPONSE },
  '/api/auth/signup': { requestFields: ENCRYPTION_FIELDS.REGISTER, responseFields: [] },
  '/api/auth/oauth2/google': { requestFields: [], responseFields: ENCRYPTION_FIELDS.USER_RESPONSE },
  '/api/auth/refresh': { requestFields: [], responseFields: ENCRYPTION_FIELDS.USER_RESPONSE },
  '/api/auth/user/me': { requestFields: [], responseFields: ENCRYPTION_FIELDS.USER_RESPONSE },
  '/api/auth/user/update': { requestFields: ENCRYPTION_FIELDS.PROFILE_UPDATE, responseFields: ENCRYPTION_FIELDS.USER_RESPONSE },

  // Account Service Endpoints
  '/api/accounts': {
    requestFields: ENCRYPTION_FIELDS.CREATE_ACCOUNT,
    responseFields: ENCRYPTION_FIELDS.ACCOUNT_RESPONSE
  },
  '/api/accounts/*': {
    requestFields: [],
    responseFields: ENCRYPTION_FIELDS.ACCOUNT_RESPONSE
  },

  // Transaction Service Endpoints
  '/api/transactions/deposit': {
    requestFields: ENCRYPTION_FIELDS.DEPOSIT_REQUEST,
    responseFields: ENCRYPTION_FIELDS.TRANSACTION_RESPONSE
  },
  '/api/transactions/withdraw': {
    requestFields: ENCRYPTION_FIELDS.WITHDRAW_REQUEST,
    responseFields: ENCRYPTION_FIELDS.TRANSACTION_RESPONSE
  },
  '/api/transactions/transfer': {
    requestFields: ENCRYPTION_FIELDS.TRANSFER_REQUEST,
    responseFields: ENCRYPTION_FIELDS.TRANSACTION_RESPONSE
  },
  '/api/transactions/history/*': {
    requestFields: [],
    responseFields: ENCRYPTION_FIELDS.TRANSACTION_RESPONSE
  },
  '/api/transactions/*': {
    requestFields: [],
    responseFields: ENCRYPTION_FIELDS.TRANSACTION_RESPONSE
  }
};

// Check if endpoint should be encrypted
function shouldEncryptEndpoint(url) {
  // Check for exact match first
  if (ENCRYPTED_ENDPOINTS[url]) {
    return ENCRYPTED_ENDPOINTS[url];
  }

  // Check for pattern matches
  for (const pattern in ENCRYPTED_ENDPOINTS) {
    if (url.includes(pattern) || url.match(new RegExp(pattern.replace(/\//g, '\\/').replace(/\*/g, '.*')))) {
      return ENCRYPTED_ENDPOINTS[pattern];
    }
  }

  return null;
}

// Request interceptor for encryption
export const encryptionRequestInterceptor = async (config) => {
  if (!cryptoUtils.isEncryptionEnabled()) {
    return config;
  }

  const encryptionConfig = shouldEncryptEndpoint(config.url);

  if (encryptionConfig && encryptionConfig.requestFields.length > 0 && config.data) {
    try {
      // Set headers to indicate encryption support and encrypted request
      config.headers['X-Supports-Encryption'] = 'true';
      config.headers['X-Request-Encrypted'] = 'true';

      // Encrypt the request data
      const encryptedData = await cryptoUtils.encryptObject(config.data, encryptionConfig.requestFields);
      config.data = encryptedData;

      console.log('Request encrypted for:', config.url);
    } catch (error) {
      console.error('Failed to encrypt request:', error);
      // Continue with unencrypted request
      config.headers['X-Request-Encrypted'] = 'false';
    }
  } else {
    // Always send encryption support header
    config.headers['X-Supports-Encryption'] = 'true';
    config.headers['X-Request-Encrypted'] = 'false';
  }

  return config;
};

// Response interceptor for decryption
export const decryptionResponseInterceptor = async (response) => {
  if (!cryptoUtils.isEncryptionEnabled()) {
    return response;
  }

  const encryptionConfig = shouldEncryptEndpoint(response.config.url);
  const isResponseEncrypted = response.headers['x-response-encrypted'] === 'true';

  if (encryptionConfig && encryptionConfig.responseFields.length > 0 && isResponseEncrypted && response.data) {
    try {
      // Decrypt the response data
      const decryptedData = await cryptoUtils.decryptObject(response.data, encryptionConfig.responseFields);
      response.data = decryptedData;

      console.log('Response decrypted for:', response.config.url);
    } catch (error) {
      console.error('Failed to decrypt response:', error);
      // Continue with encrypted response (might cause issues, but better than crashing)
    }
  }

  return response;
};

// Error interceptor for encrypted responses
export const decryptionErrorInterceptor = async (error) => {
  if (error.response && cryptoUtils.isEncryptionEnabled()) {
    const encryptionConfig = shouldEncryptEndpoint(error.response.config.url);
    const isResponseEncrypted = error.response.headers['x-response-encrypted'] === 'true';

    if (encryptionConfig && isResponseEncrypted && error.response.data) {
      try {
        // Try to decrypt error response
        const decryptedData = await cryptoUtils.decryptObject(
          error.response.data,
          ['message', 'error'] // Common error fields
        );
        error.response.data = decryptedData;
      } catch (decryptError) {
        console.error('Failed to decrypt error response:', decryptError);
      }
    }
  }

  return Promise.reject(error);
};