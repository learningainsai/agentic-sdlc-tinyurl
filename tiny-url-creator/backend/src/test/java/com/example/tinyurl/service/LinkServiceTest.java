package com.example.tinyurl.service;

import com.example.tinyurl.config.AppProperties;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** Unit tests for {@link LinkService} covering TEST-001..005/007. */
@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

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
        return new LinkService(repository, codeGenerator, properties());
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
}
