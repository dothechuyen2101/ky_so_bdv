package com.bidv.t24r.signing.ingestion;

import com.bidv.t24r.signing.outbox.OutboxEventService;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {
    private final OutboxEventService outboxEventService;

    public IngestionService(OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    public IngestionRunResponse run(IngestionRequest request) {
        Instant startedAt = Instant.now();
        int enqueued = outboxEventService.enqueueFromCore(request.businessDate(), request.resolvedLimit());
        return new IngestionRunResponse(
                startedAt,
                Instant.now(),
                enqueued
        );
    }
}
