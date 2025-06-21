package com.maxiflexy.auth_service.util;

import com.maxiflexy.auth_service.service.EncryptionService;

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