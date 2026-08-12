package com.bidv.t24r.signing.document;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String id) {
        super("Digital document not found: " + id);
    }
}
