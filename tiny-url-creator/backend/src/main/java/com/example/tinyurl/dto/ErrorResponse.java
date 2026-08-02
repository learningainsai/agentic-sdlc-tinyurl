package com.example.tinyurl.dto;

import java.time.Instant;

/** Uniform error contract across all endpoints (ADR-009); never exposes stack traces. */
public record ErrorResponse(Instant timestamp, int status, String error, String message) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message);
    }
}
