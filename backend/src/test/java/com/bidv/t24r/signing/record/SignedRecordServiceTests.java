package com.bidv.t24r.signing.record;

import com.bidv.t24r.signing.document.DocumentQueryService;
import com.bidv.t24r.signing.document.DocumentSearchCriteria;
import com.bidv.t24r.signing.document.DocumentStatus;
import com.bidv.t24r.signing.ingestion.IngestionRequest;
import com.bidv.t24r.signing.ingestion.IngestionService;
import com.bidv.t24r.signing.outbox.OutboxEventService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "app.storage.root-path=target/test-digital-signing")
class SignedRecordServiceTests {
    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private OutboxEventService outboxEventService;

    @Autowired
    private DocumentQueryService documentQueryService;

    @Test
    void searchReturnsSignedDocumentsByStructuredMetadata() {
        ingestionService.run(new IngestionRequest(LocalDate.of(2026, 1, 15), 2));
        outboxEventService.pollOnly(2);

        var response = documentQueryService.search(
                new DocumentSearchCriteria(null, null, null, null, null, null, "HCM02",
                        DocumentStatus.SIGNED, null, null, null, null, null, null, null, null, null),
                0,
                10
        );

        assertThat(response.items()).isNotEmpty();
        assertThat(response.items().getFirst().status()).isEqualTo(DocumentStatus.SIGNED);
    }

    @Test
    void searchSupportsCaseInsensitivePartialTextValues() {
        ingestionService.run(new IngestionRequest(LocalDate.of(2026, 1, 16), 2));
        outboxEventService.pollOnly(2);

        var response = documentQueryService.search(
                new DocumentSearchCriteria("01-16-0001", null, "if00100", null, "term", null, "hcm",
                        DocumentStatus.SIGNED, null, null, null, null, null, null, null, null, null),
                0,
                10
        );

        assertThat(response.items())
                .extracting(item -> item.transactionId())
                .contains("T24R-2026-01-16-0001");
    }
}
