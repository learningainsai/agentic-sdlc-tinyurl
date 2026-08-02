package com.example.tinyurl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Full-stack integration test: create then redirect (TEST-006 end-to-end, REQ-001/005/006). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LinkFlowIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate rest;

    @LocalServerPort
    private int port;

    private String base() {
        return "http://localhost:" + port;
    }

    @Test
    void createThenRedirect() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"url\":\"https://example.com/page\"}", headers);

        ResponseEntity<String> created = rest.postForEntity(base() + "/api/links", request, String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode body = objectMapper.readTree(created.getBody());
        String code = body.get("code").asText();
        assertThat(code).isNotBlank();

        // TestRestTemplate does not auto-follow redirects, so we can assert the 302 + Location.
        ResponseEntity<Void> redirect = rest.exchange(
                base() + "/" + code, HttpMethod.GET, HttpEntity.EMPTY, Void.class);
        assertThat(redirect.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(redirect.getHeaders().getLocation()).hasToString("https://example.com/page");
    }
}
