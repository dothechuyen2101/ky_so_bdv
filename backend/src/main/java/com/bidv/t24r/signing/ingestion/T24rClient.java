package com.bidv.t24r.signing.ingestion;

import java.time.LocalDate;
import java.util.List;

public interface T24rClient {
    List<T24rRawRecord> fetchPendingRecords(LocalDate businessDate, int limit);
}
