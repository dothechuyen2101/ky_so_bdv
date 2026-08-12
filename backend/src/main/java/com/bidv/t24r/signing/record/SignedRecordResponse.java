package com.bidv.t24r.signing.record;

import java.time.Instant;

public record SignedRecordResponse(
        String id,
        String sourceSystem,
        String businessKey,
        String customerId,
        String customerName,
        String accountNo,
        String productCode,
        String branchCode,
        String recordType,
        SigningStatus status,
        Instant ingestedAt,
        Instant signedAt,
        String payloadHash,
        String certificateSerial,
        String signer,
        String signatureTimestamp,
        Long versionNumber
) {
    public static SignedRecordResponse from(SignedRecord record) {
        return new SignedRecordResponse(
                record.getId(),
                record.getSourceSystem(),
                record.getBusinessKey(),
                record.getCustomerId(),
                record.getCustomerName(),
                record.getAccountNo(),
                record.getProductCode(),
                record.getBranchCode(),
                record.getRecordType(),
                record.getStatus(),
                record.getIngestedAt(),
                record.getSignedAt(),
                record.getPayloadHash(),
                record.getCertificateSerial(),
                record.getSigner(),
                record.getSignatureTimestamp(),
                record.getVersionNumber()
        );
    }
}
