package com.example.tinyurl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Infrastructure beans: the rate limiter, the create-endpoint filter, and the expiry clock. */
@Configuration
public class WebConfig {

    /** UTC clock; injected so expiry checks are deterministic in tests (ADR-011). */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /** Shared per-IP rate limiter used by both the create filter and the bulk path (ADR-014). */
    @Bean
    public RateLimiter rateLimiter(AppProperties properties) {
        return new RateLimiter(properties);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimiter rateLimiter, ObjectMapper objectMapper) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(new RateLimitFilter(rateLimiter, objectMapper));
        registration.addUrlPatterns("/api/links");
        registration.setOrder(1);
        return registration;
    }
}
