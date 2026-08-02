package com.example.tinyurl.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request body for bulk short-link creation (REQ-015/017, ADR-013). Size is validated up front
 * (empty or &gt;100 -&gt; 400). {@code @Valid} is intentionally NOT cascaded into items so that a
 * single malformed item yields a per-item error rather than rejecting the whole batch (D6).
 */
public class BulkCreateRequest {

    @NotNull(message = "items is required")
    @Size(min = 1, max = 100, message = "items must contain between 1 and 100 entries")
    private List<CreateLinkRequest> items;

    public BulkCreateRequest() {
    }

    public BulkCreateRequest(List<CreateLinkRequest> items) {
        this.items = items;
    }

    public List<CreateLinkRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateLinkRequest> items) {
        this.items = items;
    }
}
