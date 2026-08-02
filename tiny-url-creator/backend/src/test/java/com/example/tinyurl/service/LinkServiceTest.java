package com.example.tinyurl.service;

import com.example.tinyurl.config.AppProperties;
import com.example.tinyurl.dto.BulkCreateRequest;
import com.example.tinyurl.dto.BulkCreateResponse;
import com.example.tinyurl.dto.CreateLinkRequest;
import com.example.tinyurl.dto.LinkResponse;
import com.example.tinyurl.entity.ShortLink;
import com.example.tinyurl.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** Unit tests for {@link LinkService} covering TEST-001..005/007 and TEST-010..016. */
@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    // Fixed "now" so expiry boundaries are deterministic (TEST-010..012).
    private static final Instant NOW = Instant.parse("2026-08-02T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private LinkRepository repository;
    @Mock
    private CodeGenerator codeGenerator;

    private AppProperties properties() {
        AppProperties props = new AppProperties();
        props.setBaseUrl("http://localhost:8080");
        return props;
    }

    private LinkService service() {
        return new LinkService(repository, codeGenerator, properties(), clock);
    }

    // TEST-001: create without alias -> generated code + short URL.
    @Test
    void createsShortLinkWithGeneratedCode() {
        when(codeGenerator.generate()).thenReturn("Abc1234");
        when(repository.existsByCode("Abc1234")).thenReturn(false);
        when(repository.save(any(ShortLink.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = service().createLink(new CreateLinkRequest("https://example.com", null));

        assertThat(response.code()).isEqualTo("Abc1234");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/Abc1234");
        assertThat(response.originalUrl()).isEqualTo("https://example.com");
    }

    // TEST-002: create with a valid custom alias.
    @Test
    void createsShortLinkWithCustomAlias() {
        when(repository.existsByCode("my-alias")).thenReturn(false);
        when(repository.save(any(ShortLink.class))).thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = service().createLink(new CreateLinkRequest("https://example.com", "my-alias"));

        ArgumentCaptor<ShortLink> captor = ArgumentCaptor.forClass(ShortLink.class);
        assertThat(response.code()).isEqualTo("my-alias");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/my-alias");
    }

    @Test
    void createsShortLinkWithExpirationDate() {
        when(repository.existsByCode("exp-123")).thenReturn(false);
        when(repository.save(any(ShortLink.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateLinkRequest request = new CreateLinkRequest("https://example.com", "exp-123");
        request.setExpiresAt(Instant.parse("2026-09-01T10:00:00Z"));

        LinkResponse response = service().createLink(request);

        assertThat(response.code()).isEqualTo("exp-123");
        assertThat(response.expiresAt()).isEqualTo("2026-09-01T10:00:00Z");
    }

    // TEST-003: duplicate alias -> 409 (AliasAlreadyExistsException).
    @Test
    void rejectsDuplicateAlias() {
        when(repository.existsByCode("taken")).thenReturn(true);

        assertThatThrownBy(() -> service().createLink(new CreateLinkRequest("https://example.com", "taken")))
                .isInstanceOf(AliasAlreadyExistsException.class);
    }

    // TEST-003: reserved-word alias -> 400 (InvalidAliasException).
    @Test
    void rejectsReservedWordAlias() {
        assertThatThrownBy(() -> service().createLink(new CreateLinkRequest("https://example.com", "api")))
                .isInstanceOf(InvalidAliasException.class);
    }

    // TEST-004: invalid URL scheme -> 400.
    @Test
    void rejectsNonHttpScheme() {
        assertThatThrownBy(() -> service().createLink(new CreateLinkRequest("ftp://example.com", null)))
                .isInstanceOf(InvalidUrlException.class);
    }

    // TEST-004: self-host URL -> 400 (REQ-010).
    @Test
    void rejectsSelfHostUrl() {
        assertThatThrownBy(() -> service().createLink(new CreateLinkRequest("http://localhost:8080/abc", null)))
                .isInstanceOf(InvalidUrlException.class);
    }

    // TEST-005: generated-code collision retries then succeeds.
    @Test
    void retriesOnGeneratedCodeCollision() {
        when(codeGenerator.generate()).thenReturn("Dup1234", "New5678");
        when(repository.existsByCode("Dup1234")).thenReturn(true);
        when(repository.existsByCode("New5678")).thenReturn(false);
        when(repository.save(any(ShortLink.class))).thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = service().createLink(new CreateLinkRequest("https://example.com", null));

        assertThat(response.code()).isEqualTo("New5678");
    }

    // TEST-007: unknown code -> CodeNotFoundException (404).
    @Test
    void resolveThrowsWhenCodeUnknown() {
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().resolve("missing"))
                .isInstanceOf(CodeNotFoundException.class);
    }

    // TEST-006 support: resolve returns the stored original URL.
    @Test
    void resolveReturnsOriginalUrl() {
        when(repository.findByCode("Abc1234"))
                .thenReturn(Optional.of(new ShortLink("Abc1234", "https://example.com", false)));

        assertThat(service().resolve("Abc1234")).isEqualTo("https://example.com");
    }

    // TEST-012: creating with a past expiry -> 400 (InvalidExpiryException).
    @Test
    void rejectsPastExpiryOnCreate() {
        CreateLinkRequest request = new CreateLinkRequest("https://example.com", null);
        request.setExpiresAt(NOW.minusSeconds(60));

        assertThatThrownBy(() -> service().createLink(request))
                .isInstanceOf(InvalidExpiryException.class);
    }

    // TEST-012: expiry exactly equal to now is not in the future -> 400.
    @Test
    void rejectsPresentExpiryOnCreate() {
        CreateLinkRequest request = new CreateLinkRequest("https://example.com", null);
        request.setExpiresAt(NOW);

        assertThatThrownBy(() -> service().createLink(request))
                .isInstanceOf(InvalidExpiryException.class);
    }

    // TEST-010: an expired link resolves as not found -> 404 (lazy expiry).
    @Test
    void resolveThrowsWhenLinkExpired() {
        ShortLink expired = new ShortLink("Exp1234", "https://example.com", false, NOW.minusSeconds(1));
        when(repository.findByCode("Exp1234")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service().resolve("Exp1234"))
                .isInstanceOf(CodeNotFoundException.class);
    }

    // TEST-011: a link not yet expired resolves normally.
    @Test
    void resolveReturnsUrlWhenNotYetExpired() {
        ShortLink live = new ShortLink("Liv1234", "https://example.com", false, NOW.plusSeconds(3600));
        when(repository.findByCode("Liv1234")).thenReturn(Optional.of(live));

        assertThat(service().resolve("Liv1234")).isEqualTo("https://example.com");
    }

    // TEST-013: bulk creates multiple valid items, results in request order.
    @Test
    void bulkCreatesAllValidItems() {
        when(codeGenerator.generate()).thenReturn("Gen0001", "Gen0002");
        when(repository.findExistingCodes(any())).thenReturn(Set.of());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        BulkCreateResponse response = service().createBulk(new BulkCreateRequest(List.of(
                new CreateLinkRequest("https://a.com", null),
                new CreateLinkRequest("https://b.com", null))));

        assertThat(response.results()).hasSize(2);
        assertThat(response.results().get(0).index()).isEqualTo(0);
        assertThat(response.results().get(0).code()).isEqualTo("Gen0001");
        assertThat(response.results().get(0).error()).isNull();
        assertThat(response.results().get(1).index()).isEqualTo(1);
        assertThat(response.results().get(1).code()).isEqualTo("Gen0002");
    }

    // TEST-014: bulk is best-effort — one invalid item fails without aborting the others.
    @Test
    void bulkReportsPerItemErrorAndKeepsValidOnes() {
        when(codeGenerator.generate()).thenReturn("Gen0001");
        when(repository.findExistingCodes(any())).thenReturn(Set.of());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        BulkCreateResponse response = service().createBulk(new BulkCreateRequest(List.of(
                new CreateLinkRequest("https://a.com", null),
                new CreateLinkRequest("ftp://bad.com", null))));

        assertThat(response.results()).hasSize(2);
        assertThat(response.results().get(0).code()).isEqualTo("Gen0001");
        assertThat(response.results().get(1).code()).isNull();
        assertThat(response.results().get(1).error().code()).isEqualTo("INVALID_URL");
    }

    // TEST-015: intra-batch duplicate alias -> first wins, loser gets ALIAS_TAKEN.
    @Test
    void bulkReportsDuplicateAliasWithinBatch() {
        when(repository.findExistingCodes(any())).thenReturn(Set.of());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        BulkCreateResponse response = service().createBulk(new BulkCreateRequest(List.of(
                new CreateLinkRequest("https://a.com", "dup"),
                new CreateLinkRequest("https://b.com", "dup"))));

        assertThat(response.results().get(0).code()).isEqualTo("dup");
        assertThat(response.results().get(1).error().code()).isEqualTo("ALIAS_TAKEN");
    }

    // TEST-016: bulk enforces the future-expiry rule per item.
    @Test
    void bulkReportsInvalidExpiryPerItem() {
        when(codeGenerator.generate()).thenReturn("Gen0001");
        when(repository.findExistingCodes(any())).thenReturn(Set.of());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CreateLinkRequest expired = new CreateLinkRequest("https://b.com", null);
        expired.setExpiresAt(NOW.minusSeconds(1));
        BulkCreateResponse response = service().createBulk(new BulkCreateRequest(List.of(
                new CreateLinkRequest("https://a.com", null),
                expired)));

        assertThat(response.results().get(0).code()).isEqualTo("Gen0001");
        assertThat(response.results().get(1).error().code()).isEqualTo("INVALID_EXPIRY");
    }
}
