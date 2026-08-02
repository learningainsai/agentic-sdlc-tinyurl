package com.example.tinyurl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers the per-IP {@link RateLimitFilter} for the link-creation endpoint only. */
@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            AppProperties properties, ObjectMapper objectMapper) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(new RateLimitFilter(properties, objectMapper));
        registration.addUrlPatterns("/api/links");
        registration.setOrder(1);
        return registration;
    }
}
