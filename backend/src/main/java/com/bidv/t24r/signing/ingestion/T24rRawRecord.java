package com.bidv.t24r.signing.ingestion;

import java.time.LocalDate;
import java.util.Map;

public record T24rRawRecord(
        String businessKey,
        String customerId,
        String customerName,
        String accountNo,
        String productCode,
        String branchCode,
        String recordType,
        LocalDate businessDate,
        Map<String, Object> payload
) {
}
