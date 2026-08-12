package com.bidv.t24r.signing.storage;

public record StoredFile(
        String relativePath,
        String sha256,
        long size,
        String mimeType
) {
}
