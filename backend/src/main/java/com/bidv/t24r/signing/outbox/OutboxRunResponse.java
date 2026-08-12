package com.bidv.t24r.signing.outbox;

import com.bidv.t24r.signing.document.DocumentResponse;
import java.time.Instant;
import java.util.List;

public record OutboxRunResponse(
        Instant startedAt,
        Instant finishedAt,
        int enqueued,
        int processed,
        int succeeded,
        int failed,
        int deadLettered,
        List<DocumentResponse> documents
) {
}
