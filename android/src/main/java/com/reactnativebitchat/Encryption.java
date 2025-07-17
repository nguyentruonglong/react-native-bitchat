package com.reactnativebitchat;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Encryption {
    public static byte[] deriveChannelKey(String password, String channel) {
        try {
            String algorithm = "PBKDF2WithHmacSHA256";
            int keyLength = 32; // 32 bytes = 256 bits
            int iterations = 100000;
            byte[] salt = channel.getBytes();

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }
}