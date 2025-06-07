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
//import java.math.BigDecimal;
//
//@Component
//public class BigDecimalEncryptionDeserializer extends JsonDeserializer<BigDecimal> {
//
//    private static EncryptionService encryptionService;
//
//    @Autowired
//    public void setEncryptionService(EncryptionService encryptionService) {
//        BigDecimalEncryptionDeserializer.encryptionService = encryptionService;
//    }
//
//    @Override
//    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//        String encryptedValue = p.getValueAsString();
//        if (encryptedValue == null || encryptedValue.isEmpty()) {
//            return null;
//        }
//        String decrypted = encryptionService.decrypt(encryptedValue);
//        return new BigDecimal(decrypted);
//    }
//}