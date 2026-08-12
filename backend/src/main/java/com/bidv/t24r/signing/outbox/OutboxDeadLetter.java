package com.bidv.t24r.signing.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_dead_letter")
public class OutboxDeadLetter {
    @Id
    private String id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public static OutboxDeadLetter from(OutboxEvent event) {
        OutboxDeadLetter deadLetter = new OutboxDeadLetter();
        deadLetter.eventId = event.getEventId();
        deadLetter.sourceSystem = event.getSourceSystem();
        deadLetter.eventType = event.getEventType();
        deadLetter.payload = event.getPayload();
        deadLetter.errorMessage = event.getErrorMessage();
        deadLetter.retryCount = event.getRetryCount();
        return deadLetter;
    }
}
