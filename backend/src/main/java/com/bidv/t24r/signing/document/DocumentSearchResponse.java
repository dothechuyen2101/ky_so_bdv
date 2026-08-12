package com.bidv.t24r.signing.document;

import java.util.List;

public record DocumentSearchResponse(
        List<DocumentResponse> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}
