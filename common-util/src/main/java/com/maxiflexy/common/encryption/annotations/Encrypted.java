package com.maxiflexy.common.encryption.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxiflexy.common.encryption.serializers.EncryptionSerializer;
import com.maxiflexy.common.encryption.serializers.EncryptionDeserializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = EncryptionSerializer.class)
@JsonDeserialize(using = EncryptionDeserializer.class)
public @interface Encrypted {
}