// File: moniebank-frontend/src/utils/cryptoUtils.js

class CryptoUtils {
  constructor() {
    // Get encryption key from environment variables
    this.secretKey = import.meta.env.VITE_ENCRYPTION_SECRET_KEY;
    if (!this.secretKey) {
      console.warn('VITE_ENCRYPTION_SECRET_KEY not found. Encryption disabled.');
      this.encryptionEnabled = false;
    } else {
      this.encryptionEnabled = true;
    }
  }

  // Convert base64 to ArrayBuffer
  base64ToArrayBuffer(base64) {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes.buffer;
  }

  // Convert ArrayBuffer to base64
  arrayBufferToBase64(buffer) {
    const bytes = new Uint8Array(buffer);
    let binaryString = '';
    for (let i = 0; i < bytes.byteLength; i++) {
      binaryString += String.fromCharCode(bytes[i]);
    }
    return btoa(binaryString);
  }

  // Get crypto key
  async getCryptoKey() {
    const keyData = this.base64ToArrayBuffer(this.secretKey);
    return await crypto.subtle.importKey(
      'raw',
      keyData,
      { name: 'AES-GCM' },
      false,
      ['encrypt', 'decrypt']
    );
  }

  // Encrypt string
  async encrypt(plainText) {
    if (!this.encryptionEnabled || !plainText) {
      return plainText;
    }

    try {
      const key = await this.getCryptoKey();
      const iv = crypto.getRandomValues(new Uint8Array(12)); // 12 bytes for GCM
      const encodedText = new TextEncoder().encode(plainText);

      const encrypted = await crypto.subtle.encrypt(
        {
          name: 'AES-GCM',
          iv: iv,
          tagLength: 128
        },
        key,
        encodedText
      );

      // Combine IV and encrypted data
      const combined = new Uint8Array(iv.length + encrypted.byteLength);
      combined.set(iv);
      combined.set(new Uint8Array(encrypted), iv.length);

      return this.arrayBufferToBase64(combined.buffer);
    } catch (error) {
      console.error('Encryption error:', error);
      return plainText; // Return original text if encryption fails
    }
  }

  // Decrypt string
  async decrypt(encryptedText) {
    if (!this.encryptionEnabled || !encryptedText) {
      return encryptedText;
    }

    try {
      const key = await this.getCryptoKey();
      const combined = this.base64ToArrayBuffer(encryptedText);

      // Extract IV and encrypted data
      const iv = combined.slice(0, 12);
      const encrypted = combined.slice(12);

      const decrypted = await crypto.subtle.decrypt(
        {
          name: 'AES-GCM',
          iv: iv,
          tagLength: 128
        },
        key,
        encrypted
      );

      return new TextDecoder().decode(decrypted);
    } catch (error) {
      console.error('Decryption error:', error);
      return encryptedText; // Return original text if decryption fails
    }
  }

  // Encrypt object recursively
  async encryptObject(obj, fieldsToEncrypt = []) {
    if (!this.encryptionEnabled || !obj || typeof obj !== 'object') {
      return obj;
    }

    const encrypted = { ...obj };

    for (const field of fieldsToEncrypt) {
      if (encrypted[field] && typeof encrypted[field] === 'string') {
        encrypted[field] = await this.encrypt(encrypted[field]);
      }
    }

    return encrypted;
  }

  // Decrypt object recursively
  async decryptObject(obj, fieldsToDecrypt = []) {
    if (!this.encryptionEnabled || !obj || typeof obj !== 'object') {
      return obj;
    }

    const decrypted = { ...obj };

    for (const field of fieldsToDecrypt) {
      if (decrypted[field] && typeof decrypted[field] === 'string') {
        decrypted[field] = await this.decrypt(decrypted[field]);
      }
    }

    return decrypted;
  }

  // Check if encryption is enabled
  isEncryptionEnabled() {
    return this.encryptionEnabled;
  }

  // Enable/disable encryption
  setEncryptionEnabled(enabled) {
    this.encryptionEnabled = enabled && !!this.secretKey;
  }
}

// Create singleton instance
const cryptoUtils = new CryptoUtils();

// Define fields that should be encrypted for different API endpoints
export const ENCRYPTION_FIELDS = {
  // Auth Service
  LOGIN: ['email', 'password'],
  REGISTER: ['name', 'email', 'password'],
  PROFILE_UPDATE: ['name', 'contactAddress'],
  USER_RESPONSE: ['userId', 'name', 'email', 'id'],

  // Account Service
  CREATE_ACCOUNT: ['fullName', 'email'],
  ACCOUNT_RESPONSE: ['id', 'fullName', 'email', 'balance', 'userId'],

  // Transaction Service
  DEPOSIT_REQUEST: ['accountId', 'amount', 'description'],
  WITHDRAW_REQUEST: ['accountId', 'amount', 'description'],
  TRANSFER_REQUEST: ['fromAccountId', 'toAccountNumber', 'amount', 'description'],
  TRANSACTION_RESPONSE: ['id', 'accountId', 'amount', 'description', 'destinationAccountNumber'],

  // Common
  ID_FIELDS: ['id', 'userId', 'accountId', 'fromAccountId'], // Fields that should be treated as IDs
  AMOUNT_FIELDS: ['balance', 'amount'], // Fields that are monetary amounts
  SENSITIVE_FIELDS: ['email', 'password', 'fullName', 'name', 'description', 'toAccountNumber', 'destinationAccountNumber']
};

export default cryptoUtils;