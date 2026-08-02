package com.example.tinyurl.config;

import com.example.tinyurl.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Per-IP fixed-window rate limiter for single link creation (ADR-008/REQ-009). Delegates counting to
 * the shared {@link RateLimiter} so bulk creation shares the same per-IP budget (ADR-014).
 * Registered via {@link WebConfig} so it stays out of MVC test slices.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only throttle single link creation; bulk enforces its own N-token budget (ADR-014).
        return !("POST".equalsIgnoreCase(request.getMethod())
                && "/api/links".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = ClientIp.of(request);
        if (rateLimiter.tryAcquire(clientIp, 1)) {
            filterChain.doFilter(request, response);
        } else {
            writeTooManyRequests(response);
        }
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Rate limit exceeded. Please try again later.");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
