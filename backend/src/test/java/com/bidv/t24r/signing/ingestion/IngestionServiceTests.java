package com.bidv.t24r.signing.ingestion;

import com.bidv.t24r.signing.outbox.OutboxEventRepository;
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
class IngestionServiceTests {
    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void runSubmitsDocumentsForCheckerApprovalWithoutSigningThem() {
        IngestionRunResponse response = ingestionService.run(new IngestionRequest(LocalDate.of(2026, 1, 16), 3));

        assertThat(response.submitted()).isEqualTo(3);
        assertThat(outboxEventRepository.count()).isGreaterThanOrEqualTo(3);
    }
}
