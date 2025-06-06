package com.example.shorturl.domain.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Sha256 {
    private static final String ALGORITHM = "SHA-256";

    public static byte[] hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing SHA-256 Algorithm", e);
        }
    }

}