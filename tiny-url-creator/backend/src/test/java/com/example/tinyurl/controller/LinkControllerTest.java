package com.example.tinyurl.controller;

import com.example.tinyurl.config.GlobalExceptionHandler;
import com.example.tinyurl.config.RateLimiter;
import com.example.tinyurl.dto.BulkCreateResponse;
import com.example.tinyurl.dto.BulkItemResult;
import com.example.tinyurl.dto.LinkResponse;
import com.example.tinyurl.service.AliasAlreadyExistsException;
import com.example.tinyurl.service.InvalidUrlException;
import com.example.tinyurl.service.LinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer tests for link creation and bulk creation (TEST-001..004, TEST-013..016, TEST-019). */
@WebMvcTest(controllers = LinkController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LinkService linkService;
    @MockBean
    private RateLimiter rateLimiter;

    // TEST-001: valid create returns 201 + body.
    @Test
    void createReturns201() throws Exception {
        when(linkService.createLink(any()))
                .thenReturn(new LinkResponse("Abc1234", "http://localhost:8080/Abc1234", "https://example.com"));

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("Abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/Abc1234"));
    }

    // TEST-004: blank url fails bean validation -> 400.
    @Test
    void createReturns400OnBlankUrl() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // TEST-004: invalid alias format fails @Pattern -> 400.
    @Test
    void createReturns400OnInvalidAlias() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\",\"alias\":\"a\"}"))
                .andExpect(status().isBadRequest());
    }

    // TEST-004: invalid URL from service -> 400.
    @Test
    void createReturns400OnInvalidUrl() throws Exception {
        when(linkService.createLink(any())).thenThrow(new InvalidUrlException("url must use http or https"));

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"ftp://example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    // TEST-003: duplicate alias -> 409.
    @Test
    void createReturns409OnDuplicateAlias() throws Exception {
        when(linkService.createLink(any())).thenThrow(new AliasAlreadyExistsException("alias already in use: taken"));

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\",\"alias\":\"taken\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // TEST-013: valid bulk request returns 200 with per-item results.
    @Test
    void bulkReturns200WithResults() throws Exception {
        when(rateLimiter.tryAcquire(anyString(), anyInt())).thenReturn(true);
        when(linkService.createBulk(any())).thenReturn(new BulkCreateResponse(List.of(
                BulkItemResult.created(0, new LinkResponse("Abc1234", "http://localhost:8080/Abc1234", "https://a.com")))));

        mockMvc.perform(post("/api/links/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"url\":\"https://a.com\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].index").value(0))
                .andExpect(jsonPath("$.results[0].code").value("Abc1234"));
    }

    // TEST-016: empty items -> 400.
    @Test
    void bulkReturns400OnEmptyItems() throws Exception {
        when(rateLimiter.tryAcquire(anyString(), anyInt())).thenReturn(true);

        mockMvc.perform(post("/api/links/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // TEST-016: more than 100 items -> 400.
    @Test
    void bulkReturns400OnTooManyItems() throws Exception {
        when(rateLimiter.tryAcquire(anyString(), anyInt())).thenReturn(true);
        String items = IntStream.range(0, 101)
                .mapToObj(i -> "{\"url\":\"https://a" + i + ".com\"}")
                .collect(Collectors.joining(","));

        mockMvc.perform(post("/api/links/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[" + items + "]}"))
                .andExpect(status().isBadRequest());
    }

    // TEST-019: per-IP budget exhausted -> whole bulk rejected with 429.
    @Test
    void bulkReturns429WhenRateLimited() throws Exception {
        when(rateLimiter.tryAcquire(anyString(), anyInt())).thenReturn(false);

        mockMvc.perform(post("/api/links/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"url\":\"https://a.com\"}]}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}
