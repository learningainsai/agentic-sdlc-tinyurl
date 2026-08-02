package com.example.tinyurl.service;

import com.example.tinyurl.config.AppProperties;
import com.example.tinyurl.dto.BulkCreateRequest;
import com.example.tinyurl.dto.BulkCreateResponse;
import com.example.tinyurl.dto.BulkItemResult;
import com.example.tinyurl.dto.CreateLinkRequest;
import com.example.tinyurl.dto.LinkResponse;
import com.example.tinyurl.entity.ShortLink;
import com.example.tinyurl.repository.LinkRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Core business logic: validation, code generation with collision retry, expiry, and lookup. */
@Service
public class LinkService {

    private static final int MAX_URL_LENGTH = 2048;
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    // Reserved words prevent codes shadowing real routes/paths (ADR-006).
    private static final Set<String> RESERVED_WORDS = Set.of(
            "api", "actuator", "admin", "login", "logout", "health",
            "swagger", "static", "assets", "favicon.ico", "robots.txt");

    private final LinkRepository repository;
    private final CodeGenerator codeGenerator;
    private final AppProperties properties;
    private final Clock clock;

    public LinkService(LinkRepository repository, CodeGenerator codeGenerator,
                       AppProperties properties, Clock clock) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public LinkResponse createLink(CreateLinkRequest request) {
        return createOne(request);
    }

    /**
     * Bulk creation (ADR-013). Deliberately NOT {@code @Transactional}: best-effort partial success
     * (D6) requires each item to commit or fail independently, so items are created one at a time and
     * per-item exceptions are captured as {@link BulkItemResult} errors instead of rolling back the
     * batch. Rate limiting (N tokens) is enforced by the caller before this method runs (ADR-014).
     */
    public BulkCreateResponse createBulk(BulkCreateRequest request) {
        List<CreateLinkRequest> items = request.getItems();
        List<BulkItemResult> results = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            CreateLinkRequest item = items.get(i);
            try {
                results.add(BulkItemResult.created(i, createOne(item)));
            } catch (RuntimeException ex) {
                results.add(BulkItemResult.error(i, errorCodeFor(ex), ex.getMessage()));
            }
        }
        return new BulkCreateResponse(results);
    }

    private LinkResponse createOne(CreateLinkRequest request) {
        String normalizedUrl = validateUrl(request.getUrl());
        Instant expiresAt = validateExpiry(request.getExpiresAt());
        String alias = request.getAlias();

        ShortLink saved = (alias != null && !alias.isBlank())
                ? saveWithAlias(alias, normalizedUrl, expiresAt)
                : saveWithGeneratedCode(normalizedUrl, expiresAt);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public String resolve(String code) {
        ShortLink link = repository.findByCode(code)
                .orElseThrow(() -> new CodeNotFoundException("No link found for code: " + code));
        // Lazy expiry (ADR-011): an expired link is indistinguishable from a missing one -> 404.
        if (isExpired(link)) {
            throw new CodeNotFoundException("No link found for code: " + code);
        }
        return link.getOriginalUrl();
    }

    private boolean isExpired(ShortLink link) {
        Instant expiresAt = link.getExpiresAt();
        return expiresAt != null && !Instant.now(clock).isBefore(expiresAt);
    }

    private Instant validateExpiry(Instant expiresAt) {
        if (expiresAt == null) {
            return null;
        }
        // Reject past or present instants (D3): expiry must be strictly in the future.
        if (!expiresAt.isAfter(Instant.now(clock))) {
            throw new InvalidExpiryException("expiresAt must be in the future");
        }
        return expiresAt;
    }

    private String errorCodeFor(RuntimeException ex) {
        if (ex instanceof AliasAlreadyExistsException) {
            return "ALIAS_TAKEN";
        }
        if (ex instanceof InvalidAliasException) {
            return "INVALID_ALIAS";
        }
        if (ex instanceof InvalidExpiryException) {
            return "INVALID_EXPIRY";
        }
        if (ex instanceof InvalidUrlException) {
            return "INVALID_URL";
        }
        return "ERROR";
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("url is required");
        }
        if (url.length() > MAX_URL_LENGTH) {
            throw new InvalidUrlException("url must not exceed " + MAX_URL_LENGTH + " characters");
        }
        final URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("url is not a valid URI");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
            throw new InvalidUrlException("url must use http or https");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("url must include a host");
        }
        if (isSelfHost(host)) {
            throw new InvalidUrlException("url must not point to this shortener's own host");
        }
        return url.trim();
    }

    private boolean isSelfHost(String host) {
        String h = host.toLowerCase(Locale.ROOT);
        for (String self : properties.getSelfHosts()) {
            if (h.equals(self.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        String baseHost = hostOf(properties.getBaseUrl());
        return baseHost != null && h.equals(baseHost.toLowerCase(Locale.ROOT));
    }

    private static String hostOf(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private ShortLink saveWithAlias(String alias, String url, Instant expiresAt) {
        if (RESERVED_WORDS.contains(alias.toLowerCase(Locale.ROOT))) {
            throw new InvalidAliasException("alias is a reserved word: " + alias);
        }
        if (repository.existsByCode(alias)) {
            throw new AliasAlreadyExistsException("alias already in use: " + alias);
        }
        try {
            return repository.save(new ShortLink(alias, url, true, expiresAt));
        } catch (DataIntegrityViolationException e) {
            // Lost the race to another concurrent request for the same alias.
            throw new AliasAlreadyExistsException("alias already in use: " + alias);
        }
    }

    private ShortLink saveWithGeneratedCode(String url, Instant expiresAt) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            if (repository.existsByCode(code)) {
                continue;
            }
            try {
                return repository.save(new ShortLink(code, url, false, expiresAt));
            } catch (DataIntegrityViolationException e) {
                // Rare collision under concurrency (ADR-004 / RISK-003): retry with a new code.
            }
        }
        throw new IllegalStateException("Unable to generate a unique code after "
                + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private LinkResponse toResponse(ShortLink link) {
        String base = properties.getBaseUrl();
        String shortUrl = base.endsWith("/")
                ? base + link.getCode()
                : base + "/" + link.getCode();
        return new LinkResponse(link.getCode(), shortUrl, link.getOriginalUrl(), link.getExpiresAt());
    }
}
