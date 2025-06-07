// File: com/maxiflexy/auth_service/config/EncryptionConfig.java
package com.maxiflexy.auth_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.maxiflexy.auth_service.encryption.*;
import com.maxiflexy.auth_service.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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