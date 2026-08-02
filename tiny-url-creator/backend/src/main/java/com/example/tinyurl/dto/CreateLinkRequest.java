package com.example.tinyurl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Request body for creating a short link (REQ-001/002/004/011). */
public class CreateLinkRequest {

    @NotBlank(message = "url is required")
    @Size(max = 2048, message = "url must not exceed 2048 characters")
    private String url;

    // Optional. When present it must match the alias format (ADR-006); reserved words checked in service.
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$",
            message = "alias must be 3-30 characters using letters, digits, hyphen or underscore")
    private String alias;

    // Optional absolute UTC instant (ADR-012, REQ-011); null = never expires. Future-check in service.
    private Instant expiresAt;

    public CreateLinkRequest() {
    }

    public CreateLinkRequest(String url, String alias) {
        this.url = url;
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
