package com.maxiflexy.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = {
        "com.maxiflexy.common.service",
        "com.maxiflexy.common.config",
        "com.maxiflexy.common.encryption.serializers",
        "com.maxiflexy.common.exception"
})
public class CommonUtilAutoConfiguration {
    // This class enables auto-configuration of all common utilities
    // when the common-util dependency is added to a service
}