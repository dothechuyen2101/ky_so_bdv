package com.bidv.t24r.signing.outbox;

import java.time.LocalDate;

public record OutboxRunRequest(
        LocalDate businessDate,
        Integer limit
) {
    public int resolvedLimit() {
        if (limit == null) {
            return 20;
        }
        return Math.min(Math.max(limit, 1), 1000);
    }
}
