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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Bulk creation (ADR-013, ADR-017). Deliberately NOT {@code @Transactional} so best-effort
     * partial success (D6) is preserved: an item either commits or fails independently.
     *
     * <p>Two passes keep the DB round trips bounded (REQ-021/024) without changing behaviour
     * (REQ-022/023):
     * <ol>
     *   <li>In memory: validate every item and assign its code (custom alias, or a generated code
     *       kept unique within the batch). Invalid items become error results immediately.</li>
     *   <li>One consolidated {@link LinkRepository#findExistingCodes} SELECT (ADR-019) resolves all
     *       collisions with persisted rows at once; survivors are inserted with a single batched
     *       {@code saveAll}. If that batch loses a race and rolls back, each survivor is retried
     *       individually (ADR-021) so one collision cannot fail the rest.</li>
     * </ol>
     * Rate limiting (N tokens) is enforced by the caller before this method runs (ADR-014).
     */
    public BulkCreateResponse createBulk(BulkCreateRequest request) {
        List<CreateLinkRequest> items = request.getItems();
        BulkItemResult[] results = new BulkItemResult[items.size()];

        // Pass 1: validate + assign codes in memory. Invalid items short-circuit to error results.
        List<PendingItem> pending = new ArrayList<>();
        Set<String> assignedCodes = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            CreateLinkRequest item = items.get(i);
            try {
                pending.add(prepare(i, item, assignedCodes));
            } catch (RuntimeException ex) {
                results[i] = BulkItemResult.error(i, errorCodeFor(ex), ex.getMessage());
            }
        }

        if (!pending.isEmpty()) {
            // Single consolidated existence check for every candidate code (ADR-019).
            Set<String> existing = repository.findExistingCodes(
                    pending.stream().map(p -> p.entity.getCode()).collect(Collectors.toList()));
            List<PendingItem> toPersist = new ArrayList<>(pending.size());
            for (PendingItem p : pending) {
                if (!existing.contains(p.entity.getCode())) {
                    toPersist.add(p);
                } else if (p.customAlias) {
                    results[p.index] = BulkItemResult.error(p.index, "ALIAS_TAKEN",
                            "alias already in use: " + p.entity.getCode());
                } else {
                    // Astronomically rare generated-code clash with a persisted row: fall back to the
                    // per-item path, which retries with a fresh code (ADR-004 / RISK-003).
                    results[p.index] = createOneCatching(p.index, items.get(p.index));
                }
            }
            persistBatch(toPersist, items, results);
        }

        return new BulkCreateResponse(List.of(results));
    }

    /** Validate a single item and assign its code, enforcing uniqueness within the batch. */
    private PendingItem prepare(int index, CreateLinkRequest request, Set<String> assignedCodes) {
        String normalizedUrl = validateUrl(request.getUrl());
        Instant expiresAt = validateExpiry(request.getExpiresAt());
        String alias = request.getAlias();
        boolean custom = alias != null && !alias.isBlank();

        String code;
        if (custom) {
            if (RESERVED_WORDS.contains(alias.toLowerCase(Locale.ROOT))) {
                throw new InvalidAliasException("alias is a reserved word: " + alias);
            }
            if (!assignedCodes.add(alias)) {
                // A duplicate alias earlier in the same batch already claimed this code.
                throw new AliasAlreadyExistsException("alias already in use: " + alias);
            }
            code = alias;
        } else {
            code = generateUniqueInBatch(assignedCodes);
        }
        return new PendingItem(index, new ShortLink(code, normalizedUrl, custom, expiresAt), custom);
    }

    /** Generate a code that is unique among those already assigned in this batch. */
    private String generateUniqueInBatch(Set<String> assignedCodes) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            if (assignedCodes.add(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate a unique code after "
                + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    /** Insert survivors in one batched {@code saveAll}; on a lost race, retry each independently. */
    private void persistBatch(List<PendingItem> toPersist, List<CreateLinkRequest> items,
                              BulkItemResult[] results) {
        if (toPersist.isEmpty()) {
            return;
        }
        List<ShortLink> entities = toPersist.stream().map(p -> p.entity).collect(Collectors.toList());
        try {
            repository.saveAll(entities);
            for (PendingItem p : toPersist) {
                results[p.index] = BulkItemResult.created(p.index, toResponse(p.entity));
            }
        } catch (DataIntegrityViolationException ex) {
            // The batched insert rolled back because a concurrent request took one of the codes.
            // Retry each item on its own so partial success is preserved (ADR-021).
            for (PendingItem p : toPersist) {
                results[p.index] = createOneCatching(p.index, items.get(p.index));
            }
        }
    }

    private BulkItemResult createOneCatching(int index, CreateLinkRequest item) {
        try {
            return BulkItemResult.created(index, createOne(item));
        } catch (RuntimeException ex) {
            return BulkItemResult.error(index, errorCodeFor(ex), ex.getMessage());
        }
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

    /** A validated, code-assigned item awaiting the consolidated existence check + batched insert. */
    private static final class PendingItem {
        private final int index;
        private final ShortLink entity;
        private final boolean customAlias;

        private PendingItem(int index, ShortLink entity, boolean customAlias) {
            this.index = index;
            this.entity = entity;
            this.customAlias = customAlias;
        }
    }
}
