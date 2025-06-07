package com.maxiflexy.common.encryption.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.maxiflexy.common.config.EncryptionContext;
import com.maxiflexy.common.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EncryptionSerializer extends JsonSerializer<Object> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        EncryptionSerializer.encryptionService = encryptionService;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            // Only encrypt if encryption is enabled for this request
            if (EncryptionContext.isEncryptionEnabled()) {
                String encrypted = encryptionService.encrypt(value.toString());
                gen.writeString(encrypted);
            } else {
                gen.writeString(value.toString());
            }
        }
    }
}