package com.bidv.t24r.signing.ingestion;

import java.time.Instant;

public record IngestionRunResponse(
        Instant startedAt,
        Instant finishedAt,
        int submitted
) {
}
