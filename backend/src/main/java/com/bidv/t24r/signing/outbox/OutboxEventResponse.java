package com.bidv.t24r.signing.outbox;

import java.time.Instant;

public record OutboxEventResponse(
        String id,
        String eventId,
        String sourceSystem,
        String eventType,
        String transactionId,
        String documentType,
        String payloadHash,
        OutboxStatus status,
        int retryCount,
        String errorMessage,
        Instant createdAt,
        Instant processedAt,
        Instant updatedAt
) {
    public static OutboxEventResponse from(OutboxEvent event) {
        return new OutboxEventResponse(
                event.getId(),
                event.getEventId(),
                event.getSourceSystem(),
                event.getEventType(),
                event.getTransactionId(),
                event.getDocumentType(),
                event.getPayloadHash(),
                event.getStatus(),
                event.getRetryCount(),
                event.getErrorMessage(),
                event.getCreatedAt(),
                event.getProcessedAt(),
                event.getUpdatedAt()
        );
    }
}
