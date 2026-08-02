package com.example.tinyurl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Response body returned after creating a short link (REQ-001/002/011).
 * {@code expiresAt} is echoed when set and omitted from JSON when the link never expires (ADR-012).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkResponse(String code, String shortUrl, String originalUrl, Instant expiresAt) {

    /** Backward-compatible constructor for links that never expire. */
    public LinkResponse(String code, String shortUrl, String originalUrl) {
        this(code, shortUrl, originalUrl, null);
    }
}
