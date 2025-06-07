package com.maxiflexy.common.util;

import com.maxiflexy.common.service.EncryptionService;

public class EncryptionKeyGenerator {
    public static void main(String[] args) {
        String key = EncryptionService.generateEncryptionKey();
        System.out.println("Generated encryption key: " + key);
        System.out.println("Add this to your application.yml:");
        System.out.println("app:");
        System.out.println("  encryption:");
        System.out.println("    secret-key: " + key);
    }
}