//package com.maxiflexy.auth_service.config;
//
//import org.springframework.stereotype.Component;
//
//@Component
//public class EncryptionContext {
//    private static final ThreadLocal<Boolean> encryptionEnabled = new ThreadLocal<>();
//    private static final ThreadLocal<Boolean> decryptionEnabled = new ThreadLocal<>();
//
//    public static void setEncryptionEnabled(boolean enabled) {
//        encryptionEnabled.set(enabled);
//    }
//
//    public static boolean isEncryptionEnabled() {
//        Boolean enabled = encryptionEnabled.get();
//        return enabled != null && enabled;
//    }
//
//    public static void setDecryptionEnabled(boolean enabled) {
//        decryptionEnabled.set(enabled);
//    }
//
//    public static boolean isDecryptionEnabled() {
//        Boolean enabled = decryptionEnabled.get();
//        return enabled != null && enabled;
//    }
//
//    public static void clear() {
//        encryptionEnabled.remove();
//        decryptionEnabled.remove();
//    }
//}