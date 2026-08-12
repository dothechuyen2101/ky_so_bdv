package com.bidv.t24r.signing.outbox;

public enum OutboxStatus {
    NEW,
    PROCESSING,
    PROCESSED,
    FAILED,
    RETRYING,
    DEAD_LETTER
}
