package com.example.tinyurl.controller;

import com.example.tinyurl.config.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer tests for link creation (TEST-001/002/003/004). */
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
}
