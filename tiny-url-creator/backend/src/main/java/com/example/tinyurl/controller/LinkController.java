package com.example.tinyurl.controller;

import com.example.tinyurl.config.ClientIp;
import com.example.tinyurl.config.RateLimitExceededException;
import com.example.tinyurl.config.RateLimiter;
import com.example.tinyurl.dto.BulkCreateRequest;
import com.example.tinyurl.dto.BulkCreateResponse;
import com.example.tinyurl.dto.CreateLinkRequest;
import com.example.tinyurl.dto.LinkResponse;
import com.example.tinyurl.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Link creation endpoints (REQ-001..004, REQ-015..019). Thin controller; logic in {@link LinkService}. */
@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;
    private final RateLimiter rateLimiter;

    public LinkController(LinkService linkService, RateLimiter rateLimiter) {
        this.linkService = linkService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
        LinkResponse response = linkService.createLink(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkCreateResponse> createBulk(@Valid @RequestBody BulkCreateRequest request,
                                                         HttpServletRequest httpRequest) {
        // N-token budget check up front (ADR-014): if the per-IP budget < N, reject the whole
        // request with 429 and create nothing. Bulk is excluded from RateLimitFilter.
        int tokens = request.getItems().size();
        if (!rateLimiter.tryAcquire(ClientIp.of(httpRequest), tokens)) {
            throw new RateLimitExceededException("Rate limit exceeded for bulk request of " + tokens + " items.");
        }
        BulkCreateResponse response = linkService.createBulk(request);
        return ResponseEntity.ok(response);
    }
}
