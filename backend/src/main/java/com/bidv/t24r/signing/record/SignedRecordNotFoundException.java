package com.bidv.t24r.signing.record;

public class SignedRecordNotFoundException extends RuntimeException {
    public SignedRecordNotFoundException(String key) {
        super("Signed record not found: " + key);
    }
}
