package com.bidv.t24r.signing.document;

import java.math.BigDecimal;
import java.time.Instant;

public record DocumentResponse(
        String id,
        String documentCode,
        String sourceSystem,
        String transactionId,
        String customerId,
        String customerName,
        String accountNoMasked,
        String documentType,
        String businessType,
        String branchCode,
        BigDecimal amount,
        String currency,
        Instant transactionDate,
        Instant signedAt,
        DocumentStatus status,
        ReconciliationStatus reconciliationStatus,
        Integer versionNumber,
        String signedHash,
        String certificateSerial,
        String signerSubject,
        Long fileSize,
        String mimeType
) {
    public static DocumentResponse from(DigitalDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getDocumentCode(),
                document.getSourceSystem(),
                document.getTransactionId(),
                document.getCustomerId(),
                document.getCustomerName(),
                document.getAccountNoMasked(),
                document.getDocumentType(),
                document.getBusinessType(),
                document.getBranchCode(),
                document.getAmount(),
                document.getCurrency(),
                document.getTransactionDate(),
                document.getSignedAt(),
                document.getStatus(),
                document.getReconciliationStatus(),
                document.getVersionNumber(),
                document.getSignedHash(),
                document.getCertificateSerial(),
                document.getSignerSubject(),
                document.getFileSize(),
                document.getMimeType()
        );
    }
}
