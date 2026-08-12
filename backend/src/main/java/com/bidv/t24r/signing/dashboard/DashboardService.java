package com.bidv.t24r.signing.dashboard;

import com.bidv.t24r.signing.document.DigitalDocumentRepository;
import com.bidv.t24r.signing.document.DocumentResponse;
import com.bidv.t24r.signing.document.DocumentStatus;
import com.bidv.t24r.signing.document.ReconciliationStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final DigitalDocumentRepository repository;

    public DashboardService(DigitalDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        Map<String, Long> byBranch = new LinkedHashMap<>();
        for (Object[] row : repository.countByBranch()) {
            byBranch.put((String) row[0], (Long) row[1]);
        }
        List<DocumentResponse> recent = repository.findRecent(PageRequest.of(0, 6))
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return new DashboardSummary(
                repository.count(),
                repository.countByStatus(DocumentStatus.SIGNED),
                repository.countByStatus(DocumentStatus.FAILED),
                repository.countByReconciliationStatus(ReconciliationStatus.HASH_MISMATCH)
                        + repository.countByReconciliationStatus(ReconciliationStatus.MISSING_FILE)
                        + repository.countByReconciliationStatus(ReconciliationStatus.MISMATCHED)
                        + repository.countByReconciliationStatus(ReconciliationStatus.DUPLICATE),
                byBranch,
                recent
        );
    }

    public record DashboardSummary(
            long totalRecords,
            long signedRecords,
            long failedRecords,
            long reconciliationIssues,
            Map<String, Long> recordsByBranch,
            List<DocumentResponse> recentRecords
    ) {
    }
}
