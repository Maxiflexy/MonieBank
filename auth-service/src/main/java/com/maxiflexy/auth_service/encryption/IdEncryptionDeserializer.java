//package com.maxiflexy.auth_service.encryption;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.maxiflexy.auth_service.config.EncryptionContext;
//import com.maxiflexy.auth_service.service.EncryptionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class IdEncryptionDeserializer extends JsonDeserializer<Long> {
//
//    private static EncryptionService encryptionService;
//
//    @Autowired
//    public void setEncryptionService(EncryptionService encryptionService) {
//        IdEncryptionDeserializer.encryptionService = encryptionService;
//    }
//
//    @Override
//    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//        if (p.currentToken().isNumeric()) {
//            // If it's a number, return as is (unencrypted)
//            return p.getValueAsLong();
//        }
//
//        String value = p.getValueAsString();
//        if (value == null || value.isEmpty()) {
//            return null;
//        }
//
//        // Only decrypt if decryption is enabled for this request
//        if (EncryptionContext.isDecryptionEnabled()) {
//            try {
//                String decrypted = encryptionService.decrypt(value);
//                return Long.valueOf(decrypted);
//            } catch (Exception e) {
//                // If decryption fails, try to parse as regular long
//                try {
//                    return Long.valueOf(value);
//                } catch (NumberFormatException nfe) {
//                    throw new IOException("Cannot decrypt or parse ID: " + value, e);
//                }
//            }
//        } else {
//            try {
//                return Long.valueOf(value);
//            } catch (NumberFormatException e) {
//                throw new IOException("Cannot parse ID as Long: " + value, e);
//            }
//        }
//    }
//}