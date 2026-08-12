package com.bidv.t24r.signing.ingestion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MockT24rClient implements T24rClient {
    private final int seedRecords;

    public MockT24rClient(@Value("${app.mock.seed-records:12}") int seedRecords) {
        this.seedRecords = seedRecords;
    }

    @Override
    public List<T24rRawRecord> fetchPendingRecords(LocalDate businessDate, int limit) {
        LocalDate date = businessDate == null ? LocalDate.now() : businessDate;
        int count = Math.min(Math.max(limit, 1), seedRecords);
        List<T24rRawRecord> records = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            String businessKey = "T24R-" + date + "-" + String.format("%04d", index);
            String customerId = "CIF" + String.format("%06d", 1000 + index);
            String branch = index % 3 == 0 ? "HN01" : index % 3 == 1 ? "HCM02" : "DN03";
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("businessKey", businessKey);
            payload.put("businessDate", date.toString());
            payload.put("customerId", customerId);
            payload.put("customerName", "Khach hang " + index);
            payload.put("accountNo", "1020" + String.format("%08d", index));
            payload.put("productCode", index % 2 == 0 ? "CASA" : "TD");
            payload.put("branchCode", branch);
            payload.put("currency", index % 2 == 0 ? "VND" : "USD");
            payload.put("balance", BigDecimal.valueOf(2_500_000L + index * 125_000L));
            payload.put("recordType", index % 2 == 0 ? "ACCOUNT_BALANCE" : "TERM_DEPOSIT");
            records.add(new T24rRawRecord(
                    businessKey,
                    customerId,
                    "Khach hang " + index,
                    "1020" + String.format("%08d", index),
                    index % 2 == 0 ? "CASA" : "TD",
                    branch,
                    index % 2 == 0 ? "ACCOUNT_BALANCE" : "TERM_DEPOSIT",
                    date,
                    payload
            ));
        }
        return records;
    }
}
