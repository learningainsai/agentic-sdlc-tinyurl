package com.example.tinyurl.config;

import jakarta.servlet.http.HttpServletRequest;

/** Resolves the client IP for rate limiting, honoring a single X-Forwarded-For hop. */
public final class ClientIp {

    private ClientIp() {
    }

    public static String of(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
