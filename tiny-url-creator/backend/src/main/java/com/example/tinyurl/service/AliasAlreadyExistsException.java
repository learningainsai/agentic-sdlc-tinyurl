package com.example.tinyurl.service;

/** Thrown when a requested custom alias is already taken (REQ-003) -> HTTP 409. */
public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String message) {
        super(message);
    }
}
