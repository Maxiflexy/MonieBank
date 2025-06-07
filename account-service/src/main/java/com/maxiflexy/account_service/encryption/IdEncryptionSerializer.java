//package com.maxiflexy.account_service.encryption;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonSerializer;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import com.maxiflexy.account_service.service.EncryptionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class IdEncryptionSerializer extends JsonSerializer<Long> {
//
//    private static EncryptionService encryptionService;
//
//    @Autowired
//    public void setEncryptionService(EncryptionService encryptionService) {
//        IdEncryptionSerializer.encryptionService = encryptionService;
//    }
//
//    @Override
//    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        if (value == null) {
//            gen.writeNull();
//        } else {
//            String encrypted = encryptionService.encrypt(value.toString());
//            gen.writeString(encrypted);
//        }
//    }
//}