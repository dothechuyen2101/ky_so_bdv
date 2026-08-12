package com.bidv.t24r.signing.ingestion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public record IngestionRequest(
        LocalDate businessDate,
        @Min(1) @Max(1000) Integer limit
) {
    public int resolvedLimit() {
        return limit == null ? 20 : limit;
    }
}
