package com.example.tinyurl.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared per-IP fixed-window rate limiter (ADR-008/ADR-014). Used by both {@link RateLimitFilter}
 * (single create = 1 token) and the bulk path (N tokens). In-memory and per-instance; a distributed
 * limiter is deferred (RISK-004/RISK-014).
 */
public class RateLimiter {

    private final AppProperties properties;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(AppProperties properties) {
        this.properties = properties;
    }

    /**
     * Atomically consume {@code tokens} from the caller's current window.
     * Returns {@code true} if the budget allowed it; on rejection nothing is consumed
     * so a rejected bulk request creates nothing (ADR-014).
     */
    public boolean tryAcquire(String clientIp, int tokens) {
        long windowMillis = properties.getRateLimit().getWindowSeconds() * 1000L;
        int max = properties.getRateLimit().getMaxRequests();
        long now = System.currentTimeMillis();

        Window window = windows.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.startMillis >= windowMillis) {
                return new Window(now);
            }
            return existing;
        });

        while (true) {
            int current = window.count.get();
            if (current + tokens > max) {
                return false;
            }
            if (window.count.compareAndSet(current, current + tokens)) {
                return true;
            }
        }
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
