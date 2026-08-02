package com.example.tinyurl.config;

/** Thrown when a request exceeds the per-IP rate-limit budget (REQ-009/REQ-019) -> HTTP 429. */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
