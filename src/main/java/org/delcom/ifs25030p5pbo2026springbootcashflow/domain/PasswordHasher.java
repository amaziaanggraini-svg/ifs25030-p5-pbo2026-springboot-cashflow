package org.delcom.ifs25030p5pbo2026springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash password sederhana (SHA-256) untuk lab JDBC.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        MessageDigest digest = messageDigest(ALGORITHM);
        byte[] hashed = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashed);
    }

    public static boolean matches(String rawPassword, String passwordHash) {
        return hash(rawPassword).equals(passwordHash);
    }

    static MessageDigest messageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(algorithm + " tidak tersedia", ex);
        }
    }
}
