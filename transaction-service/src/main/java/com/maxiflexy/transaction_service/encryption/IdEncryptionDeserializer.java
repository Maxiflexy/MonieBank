//package com.maxiflexy.transaction_service.encryption;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.maxiflexy.transaction_service.service.EncryptionService;
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
//        String encryptedValue = p.getValueAsString();
//        if (encryptedValue == null || encryptedValue.isEmpty()) {
//            return null;
//        }
//        String decrypted = encryptionService.decrypt(encryptedValue);
//        return Long.valueOf(decrypted);
//    }
//}