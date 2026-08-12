package com.bidv.t24r.signing.config;

import com.bidv.t24r.signing.document.DigitalDocumentRepository;
import com.bidv.t24r.signing.ingestion.IngestionRequest;
import com.bidv.t24r.signing.ingestion.IngestionService;
import com.bidv.t24r.signing.outbox.OutboxEventService;
import com.bidv.t24r.signing.outbox.OutboxRunRequest;
import com.bidv.t24r.signing.security.ApiClientService;
import com.bidv.t24r.signing.user.UserAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    ApplicationRunner seedDemoData(
            ApiClientService apiClientService,
            DigitalDocumentRepository documentRepository,
            IngestionService ingestionService,
            OutboxEventService outboxEventService,
            UserAccountService userAccountService,
            @Value("${app.security.default-api-client-id}") String clientId,
            @Value("${app.security.default-api-key}") String apiKey,
            @Value("${app.security.admin-username:admin}") String adminUsername,
            @Value("${app.security.admin-password:admin123}") String adminPassword
    ) {
        return args -> {
            apiClientService.ensureDefaultClient(clientId, "Partner Sandbox", apiKey);
            userAccountService.ensureAdmin(adminUsername, adminPassword);
            if (documentRepository.count() == 0) {
                ingestionService.run(new IngestionRequest(null, 8));
                outboxEventService.run(new OutboxRunRequest(null, 8));
            }
        };
    }
}
