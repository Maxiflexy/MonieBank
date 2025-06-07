package com.maxiflexy.common.encryption.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.maxiflexy.common.config.EncryptionContext;
import com.maxiflexy.common.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EncryptionDeserializer extends JsonDeserializer<String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        EncryptionDeserializer.encryptionService = encryptionService;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.isEmpty()) {
            return value;
        }

        // Only decrypt if decryption is enabled for this request
        if (EncryptionContext.isDecryptionEnabled()) {
            try {
                return encryptionService.decrypt(value);
            } catch (Exception e) {
                // If decryption fails, return original value (might be unencrypted)
                return value;
            }
        } else {
            return value;
        }
    }
}