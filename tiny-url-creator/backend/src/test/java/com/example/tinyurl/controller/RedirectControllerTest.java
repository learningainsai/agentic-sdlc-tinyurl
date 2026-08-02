package com.example.tinyurl.controller;

import com.example.tinyurl.config.GlobalExceptionHandler;
import com.example.tinyurl.service.CodeNotFoundException;
import com.example.tinyurl.service.LinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer tests for redirection (TEST-006/007). */
@WebMvcTest(controllers = RedirectController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LinkService linkService;

    // TEST-006: known code -> 302 with Location header.
    @Test
    void redirectReturns302() throws Exception {
        when(linkService.resolve("Abc1234")).thenReturn("https://example.com/page");

        mockMvc.perform(get("/Abc1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/page"));
    }

    // TEST-007: unknown code -> 404.
    @Test
    void redirectReturns404OnUnknownCode() throws Exception {
        when(linkService.resolve("missing")).thenThrow(new CodeNotFoundException("No link found for code: missing"));

        mockMvc.perform(get("/missing"))
                .andExpect(status().isNotFound());
    }
}
