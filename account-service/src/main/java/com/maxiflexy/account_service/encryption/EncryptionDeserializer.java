//package com.maxiflexy.account_service.encryption;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.maxiflexy.account_service.service.EncryptionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class EncryptionDeserializer extends JsonDeserializer<String> {
//
//    private static EncryptionService encryptionService;
//
//    @Autowired
//    public void setEncryptionService(EncryptionService encryptionService) {
//        EncryptionDeserializer.encryptionService = encryptionService;
//    }
//
//    @Override
//    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//        String encryptedValue = p.getValueAsString();
//        if (encryptedValue == null || encryptedValue.isEmpty()) {
//            return encryptedValue;
//        }
//        return encryptionService.decrypt(encryptedValue);
//    }
//}