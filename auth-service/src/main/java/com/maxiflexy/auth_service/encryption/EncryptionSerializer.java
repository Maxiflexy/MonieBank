//package com.maxiflexy.auth_service.encryption;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonSerializer;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import com.maxiflexy.auth_service.config.EncryptionContext;
//import com.maxiflexy.auth_service.service.EncryptionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class EncryptionSerializer extends JsonSerializer<Object> {
//
//    private static EncryptionService encryptionService;
//
//    @Autowired
//    public void setEncryptionService(EncryptionService encryptionService) {
//        EncryptionSerializer.encryptionService = encryptionService;
//    }
//
//    @Override
//    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        if (value == null) {
//            gen.writeNull();
//        } else {
//            // Only encrypt if encryption is enabled for this request
//            if (EncryptionContext.isEncryptionEnabled()) {
//                String encrypted = encryptionService.encrypt(value.toString());
//                gen.writeString(encrypted);
//            } else {
//                gen.writeString(value.toString());
//            }
//        }
//    }
//}