package com.bidv.t24r.signing.reconciliation;

import com.bidv.t24r.signing.document.DocumentResponse;
import com.bidv.t24r.signing.document.ReconciliationStatus;
import java.time.Instant;
import java.util.List;

public record ReconciliationResponse(
        DocumentResponse document,
        ReconciliationStatus status,
        List<String> issues,
        boolean originalHashValid,
        boolean signedHashValid,
        Instant checkedAt
) {
}
