package com.maxiflexy.transaction_service.encryption;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.maxiflexy.transaction_service.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
public class BigDecimalEncryptionSerializer extends JsonSerializer<BigDecimal> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        BigDecimalEncryptionSerializer.encryptionService = encryptionService;
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            String encrypted = encryptionService.encrypt(value.toString());
            gen.writeString(encrypted);
        }
    }
}