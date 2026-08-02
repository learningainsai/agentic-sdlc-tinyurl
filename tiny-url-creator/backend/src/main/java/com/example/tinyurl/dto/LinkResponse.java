package com.example.tinyurl.dto;

/** Response body returned after creating a short link (REQ-001/002). */
public record LinkResponse(String code, String shortUrl, String originalUrl) {
}
