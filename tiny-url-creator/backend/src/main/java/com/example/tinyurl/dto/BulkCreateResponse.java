package com.example.tinyurl.dto;

import java.util.List;

/** Response body for bulk creation (ADR-013): per-item results in request order. */
public record BulkCreateResponse(List<BulkItemResult> results) {
}
