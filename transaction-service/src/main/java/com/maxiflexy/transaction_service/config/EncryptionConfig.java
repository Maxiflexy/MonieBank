package com.maxiflexy.transaction_service.config;

import com.maxiflexy.transaction_service.encryption.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Autowired
    private EncryptionSerializer encryptionSerializer;

    @Autowired
    private EncryptionDeserializer encryptionDeserializer;

    @Autowired
    private IdEncryptionSerializer idEncryptionSerializer;

    @Autowired
    private IdEncryptionDeserializer idEncryptionDeserializer;

//    @Bean
//    @Primary
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        SimpleModule encryptionModule = new SimpleModule("EncryptionModule");
//        encryptionModule.addSerializer(String.class, encryptionSerializer);
//        encryptionModule.addDeserializer(String.class, encryptionDeserializer);
//        encryptionModule.addSerializer(Long.class, idEncryptionSerializer);
//        encryptionModule.addDeserializer(Long.class, idEncryptionDeserializer);
//
//        objectMapper.registerModule(encryptionModule);
//
//        return objectMapper;
//    }
}