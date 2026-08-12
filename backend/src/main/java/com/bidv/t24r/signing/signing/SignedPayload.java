package com.bidv.t24r.signing.signing;

import java.time.Instant;

public record SignedPayload(
        String payloadHash,
        String signatureValue,
        String certificateSerial,
        String signer,
        Instant signedAt
) {
}
