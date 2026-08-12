package com.bidv.t24r.signing.record;

import java.util.List;

public record SignedRecordSearchResponse(
        List<SignedRecordResponse> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}
