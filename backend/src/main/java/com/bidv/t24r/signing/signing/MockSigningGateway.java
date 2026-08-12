package com.bidv.t24r.signing.signing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class MockSigningGateway implements SigningGateway {
    @Override
    public SignedPayload sign(String businessKey, String canonicalPayload) {
        String payloadHash = sha256(canonicalPayload);
        String signature = sha256("CQTT|" + businessKey + "|" + payloadHash);
        return new SignedPayload(
                payloadHash,
                "MOCK-CQTT-SIGNATURE:" + signature,
                "CQTT-MOCK-CERT-2026-01",
                "Cuc Cong Nghe Thong Tin",
                Instant.now()
        );
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot calculate SHA-256", ex);
        }
    }
}
