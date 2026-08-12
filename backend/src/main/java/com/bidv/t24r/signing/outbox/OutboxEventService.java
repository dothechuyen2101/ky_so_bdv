package com.bidv.t24r.signing.outbox;

import com.bidv.t24r.signing.audit.AuditService;
import com.bidv.t24r.signing.document.DocumentResponse;
import com.bidv.t24r.signing.ingestion.T24rClient;
import com.bidv.t24r.signing.ingestion.T24rRawRecord;
import com.bidv.t24r.signing.orchestrator.SigningOrchestratorService;
import com.bidv.t24r.signing.storage.LocalFileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxEventService {
    private static final String EVENT_TYPE = "DOCUMENT_READY_FOR_SIGNING";

    private final T24rClient t24rClient;
    private final ObjectMapper objectMapper;
    private final LocalFileStorageService storageService;
    private final SigningOrchestratorService orchestratorService;
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxDeadLetterRepository deadLetterRepository;
    private final AuditService auditService;
    private final int maxRetries;

    public OutboxEventService(
            T24rClient t24rClient,
            ObjectMapper objectMapper,
            LocalFileStorageService storageService,
            SigningOrchestratorService orchestratorService,
            OutboxEventRepository outboxEventRepository,
            OutboxDeadLetterRepository deadLetterRepository,
            AuditService auditService,
            @Value("${app.outbox.max-retries:3}") int maxRetries
    ) {
        this.t24rClient = t24rClient;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
        this.orchestratorService = orchestratorService;
        this.outboxEventRepository = outboxEventRepository;
        this.deadLetterRepository = deadLetterRepository;
        this.auditService = auditService;
        this.maxRetries = maxRetries;
    }

    @Transactional
    public OutboxRunResponse run(OutboxRunRequest request) {
        return poll(request.resolvedLimit(), Instant.now(), 0);
    }

    @Transactional
    public int enqueueFromCore(LocalDate businessDate, int limit) {
        List<T24rRawRecord> records = t24rClient.fetchPendingRecords(businessDate, limit);
        int enqueued = 0;
        for (T24rRawRecord record : records) {
            if (enqueue(record)) {
                enqueued++;
            }
        }
        return enqueued;
    }

    @Transactional
    public OutboxRunResponse pollOnly(int limit) {
        return poll(Math.min(Math.max(limit, 1), 1000), Instant.now(), 0);
    }

    @Transactional(readOnly = true)
    public OutboxSummaryResponse summary() {
        Map<OutboxStatus, Long> byStatus = new EnumMap<>(OutboxStatus.class);
        for (OutboxStatus status : OutboxStatus.values()) {
            byStatus.put(status, outboxEventRepository.countByStatus(status));
        }
        List<OutboxEventResponse> recent = outboxEventRepository.findRecent(PageRequest.of(0, 12))
                .stream()
                .map(OutboxEventResponse::from)
                .toList();
        return new OutboxSummaryResponse(outboxEventRepository.count(), byStatus, recent);
    }

    private boolean enqueue(T24rRawRecord record) {
        String eventId = "OUTBOX-" + record.businessKey();
        if (outboxEventRepository.findByEventId(eventId).isPresent()) {
            return false;
        }
        try {
            String payload = objectMapper.writeValueAsString(record);
            OutboxEvent event = new OutboxEvent();
            event.setEventId(eventId);
            event.setSourceSystem("T24R");
            event.setEventType(EVENT_TYPE);
            event.setAggregateType("T24R_DOCUMENT");
            event.setAggregateId(record.businessKey());
            event.setTransactionId(record.businessKey());
            event.setDocumentType(record.recordType());
            event.setPayload(payload);
            event.setPayloadHash(storageService.sha256(payload));
            event.setStatus(OutboxStatus.NEW);
            outboxEventRepository.save(event);
            auditService.record("OUTBOX_EVENT_CREATED", "system", "OutboxEvent", eventId,
                    "Created outbox event for " + record.businessKey(), null);
            return true;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot enqueue T24R outbox event " + record.businessKey(), ex);
        }
    }

    private OutboxRunResponse poll(int limit, Instant startedAt, int enqueued) {
        List<OutboxEvent> events = outboxEventRepository.findNextForUpdate(
                List.of(OutboxStatus.NEW, OutboxStatus.RETRYING),
                PageRequest.of(0, limit)
        );
        List<DocumentResponse> documents = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;
        int deadLettered = 0;
        for (OutboxEvent event : events) {
            event.setStatus(OutboxStatus.PROCESSING);
            outboxEventRepository.save(event);
            try {
                var document = orchestratorService.process(event);
                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(Instant.now());
                event.setErrorMessage(null);
                outboxEventRepository.save(event);
                documents.add(DocumentResponse.from(document));
                succeeded++;
                auditService.record("OUTBOX_EVENT_PROCESSED", "system", "OutboxEvent", event.getEventId(),
                        "Processed outbox event " + event.getEventId(), null);
            } catch (Exception ex) {
                failed++;
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(shortMessage(ex));
                if (event.getRetryCount() >= maxRetries) {
                    event.setStatus(OutboxStatus.DEAD_LETTER);
                    deadLetterRepository.save(OutboxDeadLetter.from(event));
                    deadLettered++;
                } else {
                    event.setStatus(OutboxStatus.RETRYING);
                }
                outboxEventRepository.save(event);
                auditService.record("OUTBOX_EVENT_FAILED", "system", "OutboxEvent", event.getEventId(),
                        shortMessage(ex), null);
            }
        }
        return new OutboxRunResponse(
                startedAt,
                Instant.now(),
                enqueued,
                events.size(),
                succeeded,
                failed,
                deadLettered,
                documents
        );
    }

    private String shortMessage(Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return message.length() > 900 ? message.substring(0, 900) : message;
    }
}
