package com.example.tinyurl.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates non-sequential 7-character Base62 codes from a CSPRNG (ADR-004/REQ-001).
 * ~62^7 (~3.5 trillion) code space keeps codes unpredictable and collisions rare.
 */
@Component
public class CodeGenerator {

    static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    static final int CODE_LENGTH = 7;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
