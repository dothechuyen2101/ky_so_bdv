package com.bidv.t24r.signing.signing;

public interface SigningGateway {
    SignedPayload sign(String businessKey, String canonicalPayload);
}
