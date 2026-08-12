package com.bidv.t24r.signing.record;

import java.time.Instant;

public record SignedRecordSearchCriteria(
        String query,
        String customerId,
        String branchCode,
        SigningStatus status,
        Instant from,
        Instant to
) {
}
