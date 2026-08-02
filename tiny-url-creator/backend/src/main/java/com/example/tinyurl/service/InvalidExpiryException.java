package com.example.tinyurl.service;

/** Thrown when a supplied expiry timestamp is not in the future (REQ-012) -> HTTP 400. */
public class InvalidExpiryException extends RuntimeException {
    public InvalidExpiryException(String message) {
        super(message);
    }
}
