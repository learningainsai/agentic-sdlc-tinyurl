package com.example.tinyurl.service;

/** Thrown when a custom alias is a reserved word or otherwise invalid (REQ-003) -> HTTP 400. */
public class InvalidAliasException extends RuntimeException {
    public InvalidAliasException(String message) {
        super(message);
    }
}
