package com.example.tinyurl;

import com.example.tinyurl.dto.BulkCreateRequest;
import com.example.tinyurl.dto.BulkCreateResponse;
import com.example.tinyurl.dto.CreateLinkRequest;
import com.example.tinyurl.service.LinkService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 3 (run-20260802T170000Z) regression tests for the batched bulk-creation persistence path.
 *
 * <p>TEST-019 (REQ-021/024) proves the DB round trips are bounded and independent of batch size; the
 * pre-optimization implementation issued ~2N statements (one {@code existsByCode} SELECT + one INSERT
 * per item). TEST-020 (REQ-022/023) proves the observable response contract and best-effort partial
 * success are unchanged for a mixed batch.
 */
@SpringBootTest
@ActiveProfiles("test")
class BulkPersistenceIT {

    @Autowired
    private LinkService service;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    // TEST-019: a batch of N items issues a bounded number of DB round trips (one consolidated
    // existence SELECT + batched INSERTs), not ~2N. Asserted via Hibernate Statistics (REQ-021/024).
    @Test
    void bulkCreationIssuesBoundedDatabaseRoundTrips() {
        int batchSize = 50;
        List<CreateLinkRequest> items = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            items.add(new CreateLinkRequest("https://example.com/perf/" + i, null));
        }

        Statistics stats = statistics();
        stats.clear();

        BulkCreateResponse response = service.createBulk(new BulkCreateRequest(items));

        assertThat(response.results()).hasSize(batchSize);
        assertThat(response.results()).allSatisfy(r -> assertThat(r.error()).isNull());

        long statements = stats.getPrepareStatementCount();
        // Baseline was ~2N = 100 for 50 items; batched path is a small constant (existence SELECT +
        // batched INSERTs + pooled sequence fetch), well under the item count.
        assertThat(statements)
                .as("prepared statements for a %d-item bulk create", batchSize)
                .isLessThanOrEqualTo(10);
    }

    // TEST-020: mixed batch — valid, invalid URL, intra-batch duplicate alias, and a pre-taken alias —
    // yields the same ordered per-item results as the original implementation (REQ-022/023).
    @Test
    void bulkCreationPreservesContractAndPartialSuccess() {
        service.createLink(new CreateLinkRequest("https://example.com/seeded", "seeded-alias"));

        List<CreateLinkRequest> items = List.of(
                new CreateLinkRequest("https://example.com/ok", null),      // 0 -> created
                new CreateLinkRequest("ftp://bad.example.com", null),       // 1 -> INVALID_URL
                new CreateLinkRequest("https://example.com/keep", "keep"),  // 2 -> created
                new CreateLinkRequest("https://example.com/dup", "keep"),   // 3 -> ALIAS_TAKEN (intra-batch)
                new CreateLinkRequest("https://example.com/taken", "seeded-alias")); // 4 -> ALIAS_TAKEN (persisted)

        BulkCreateResponse response = service.createBulk(new BulkCreateRequest(items));

        assertThat(response.results()).hasSize(5);

        assertThat(response.results().get(0).index()).isEqualTo(0);
        assertThat(response.results().get(0).code()).isNotBlank();
        assertThat(response.results().get(0).error()).isNull();

        assertThat(response.results().get(1).code()).isNull();
        assertThat(response.results().get(1).error().code()).isEqualTo("INVALID_URL");

        assertThat(response.results().get(2).code()).isEqualTo("keep");
        assertThat(response.results().get(2).error()).isNull();

        assertThat(response.results().get(3).code()).isNull();
        assertThat(response.results().get(3).error().code()).isEqualTo("ALIAS_TAKEN");

        assertThat(response.results().get(4).code()).isNull();
        assertThat(response.results().get(4).error().code()).isEqualTo("ALIAS_TAKEN");
    }
}
