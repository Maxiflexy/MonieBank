package com.maxiflexy.auth_service.config;

import com.maxiflexy.auth_service.encryption.*;
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

}