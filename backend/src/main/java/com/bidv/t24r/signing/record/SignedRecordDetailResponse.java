package com.bidv.t24r.signing.record;

public record SignedRecordDetailResponse(
        SignedRecordResponse metadata,
        String canonicalPayload,
        String signatureValue
) {
    public static SignedRecordDetailResponse from(SignedRecord record) {
        return new SignedRecordDetailResponse(
                SignedRecordResponse.from(record),
                record.getCanonicalPayload(),
                record.getSignatureValue()
        );
    }
}
