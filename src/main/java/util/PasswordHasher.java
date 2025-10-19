package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PasswordHasher {
    private PasswordHasher() {}
    public static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
