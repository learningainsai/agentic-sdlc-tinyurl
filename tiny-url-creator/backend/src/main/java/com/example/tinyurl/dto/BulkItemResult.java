package com.example.tinyurl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * One entry in a {@link BulkCreateResponse}, in request order (ADR-013). Either a created link
 * (code/shortUrl/originalUrl, optional expiresAt) or an {@link Error} — never both. Null fields are
 * omitted from the JSON so each item is unambiguously a success or a failure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BulkItemResult(
        int index,
        String code,
        String shortUrl,
        String originalUrl,
        Instant expiresAt,
        Error error) {

    /** Per-item failure detail. */
    public record Error(String code, String message) {
    }

    public static BulkItemResult created(int index, LinkResponse link) {
        return new BulkItemResult(index, link.code(), link.shortUrl(), link.originalUrl(),
                link.expiresAt(), null);
    }

    public static BulkItemResult error(int index, String errorCode, String message) {
        return new BulkItemResult(index, null, null, null, null, new Error(errorCode, message));
    }
}
