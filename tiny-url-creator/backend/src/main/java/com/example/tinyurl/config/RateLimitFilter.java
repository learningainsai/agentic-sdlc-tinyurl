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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-IP fixed-window rate limiter for link creation (ADR-008/REQ-009).
 * In-memory and per-instance; a distributed limiter is deferred (RISK-004).
 * Registered via {@link WebConfig} so it stays out of MVC test slices.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(AppProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only throttle link creation.
        return !("POST".equalsIgnoreCase(request.getMethod())
                && "/api/links".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = clientIp(request);
        if (isAllowed(clientIp)) {
            filterChain.doFilter(request, response);
        } else {
            writeTooManyRequests(response);
        }
    }

    private boolean isAllowed(String clientIp) {
        long windowMillis = properties.getRateLimit().getWindowSeconds() * 1000L;
        int max = properties.getRateLimit().getMaxRequests();
        long now = System.currentTimeMillis();

        Window window = windows.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.startMillis >= windowMillis) {
                return new Window(now);
            }
            return existing;
        });
        return window.count.incrementAndGet() <= max;
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
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

    /** Fixed-window counter. */
    private static final class Window {
        private final long startMillis;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long startMillis) {
            this.startMillis = startMillis;
        }
    }
}
