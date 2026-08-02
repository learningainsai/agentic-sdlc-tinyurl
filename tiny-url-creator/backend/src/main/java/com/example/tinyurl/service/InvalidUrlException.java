package com.example.tinyurl.service;

/** Thrown when the submitted URL fails validation (REQ-004/REQ-010) -> HTTP 400. */
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }
}
