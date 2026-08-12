package com.bidv.t24r.signing.document;

public enum ReconciliationStatus {
    NOT_CHECKED,
    MATCHED,
    MISMATCHED,
    MISSING_FILE,
    HASH_MISMATCH,
    DUPLICATE
}
