//package com.maxiflexy.account_service.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Cipher;
//import javax.crypto.KeyGenerator;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.GCMParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.security.SecureRandom;
//import java.util.Base64;
//
//@Service
//public class EncryptionService {
//
//    private static final String ALGORITHM = "AES";
//    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
//    private static final int GCM_IV_LENGTH = 12;
//    private static final int GCM_TAG_LENGTH = 16;
//
//    @Value("${app.encryption.secret-key}")
//    private String secretKey;
//
//    private SecretKey getSecretKey() {
//        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
//        return new SecretKeySpec(decodedKey, ALGORITHM);
//    }
//
//    public String encrypt(String plainText) {
//        if (plainText == null || plainText.isEmpty()) {
//            return plainText;
//        }
//
//        try {
//            SecretKey key = getSecretKey();
//            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//
//            byte[] iv = new byte[GCM_IV_LENGTH];
//            new SecureRandom().nextBytes(iv);
//            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
//
//            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
//            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
//
//            byte[] encryptedWithIv = new byte[iv.length + cipherText.length];
//            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
//            System.arraycopy(cipherText, 0, encryptedWithIv, iv.length, cipherText.length);
//
//            return Base64.getEncoder().encodeToString(encryptedWithIv);
//        } catch (Exception e) {
//            throw new RuntimeException("Error encrypting data", e);
//        }
//    }
//
//    public String decrypt(String encryptedText) {
//        if (encryptedText == null || encryptedText.isEmpty()) {
//            return encryptedText;
//        }
//
//        try {
//            SecretKey key = getSecretKey();
//            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//
//            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
//
//            byte[] iv = new byte[GCM_IV_LENGTH];
//            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
//
//            byte[] cipherText = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
//            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
//
//            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
//            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
//
//            byte[] plainText = cipher.doFinal(cipherText);
//            return new String(plainText, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new RuntimeException("Error decrypting data", e);
//        }
//    }
//
//    public static String generateEncryptionKey() {
//        try {
//            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
//            keyGenerator.init(256);
//            SecretKey secretKey = keyGenerator.generateKey();
//            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating encryption key", e);
//        }
//    }
//}