package com.maxiflexy.common.config;

import com.maxiflexy.common.encryption.serializers.*;
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

    @Autowired
    private BigDecimalEncryptionSerializer bigDecimalEncryptionSerializer;

    @Autowired
    private BigDecimalEncryptionDeserializer bigDecimalEncryptionDeserializer;
}