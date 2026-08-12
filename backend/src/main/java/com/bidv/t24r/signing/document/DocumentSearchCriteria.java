package com.bidv.t24r.signing.document;

import java.math.BigDecimal;
import java.time.Instant;

public record DocumentSearchCriteria(
        String transactionId,
        String documentCode,
        String customerId,
        String accountNoMasked,
        String documentType,
        String businessType,
        String branchCode,
        DocumentStatus status,
        ReconciliationStatus reconciliationStatus,
        Instant transactionFrom,
        Instant transactionTo,
        Instant signedFrom,
        Instant signedTo,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String hash,
        String signer
) {
}
