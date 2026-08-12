package com.bidv.t24r.signing.document;

public record DocumentDetailResponse(
        DocumentResponse metadata,
        String originalFilePath,
        String signedFilePath,
        String originalHash,
        String signedHash,
        String signatureAlgorithm,
        String timestampTokenId,
        String dataSnapshot,
        String signatureValue
) {
    public static DocumentDetailResponse from(DigitalDocument document) {
        return new DocumentDetailResponse(
                DocumentResponse.from(document),
                document.getOriginalFilePath(),
                document.getSignedFilePath(),
                document.getOriginalHash(),
                document.getSignedHash(),
                document.getSignatureAlgorithm(),
                document.getTimestampTokenId(),
                document.getDataSnapshot(),
                document.getSignatureValue()
        );
    }
}
