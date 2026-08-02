package com.example.tinyurl.service;

/** Thrown when a short code cannot be found for redirection (REQ-006) -> HTTP 404. */
public class CodeNotFoundException extends RuntimeException {
    public CodeNotFoundException(String message) {
        super(message);
    }
}
