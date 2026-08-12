package com.bidv.t24r.signing.reconciliation;

import com.bidv.t24r.signing.audit.AuditService;
import com.bidv.t24r.signing.document.DigitalDocument;
import com.bidv.t24r.signing.document.DigitalDocumentRepository;
import com.bidv.t24r.signing.document.DocumentNotFoundException;
import com.bidv.t24r.signing.document.DocumentResponse;
import com.bidv.t24r.signing.document.ReconciliationStatus;
import com.bidv.t24r.signing.storage.LocalFileStorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReconciliationService {
    private final DigitalDocumentRepository documentRepository;
    private final LocalFileStorageService storageService;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public ReconciliationService(
            DigitalDocumentRepository documentRepository,
            LocalFileStorageService storageService,
            ObjectMapper objectMapper,
            AuditService auditService
    ) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Transactional
    public ReconciliationResponse reconcile(String documentId) {
        DigitalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        return reconcile(document);
    }

    @Transactional
    public List<ReconciliationResponse> reconcilePending(int limit) {
        return documentRepository.findByReconciliationStatus(
                        ReconciliationStatus.NOT_CHECKED,
                        PageRequest.of(0, Math.min(Math.max(limit, 1), 1000)))
                .stream()
                .map(this::reconcile)
                .toList();
    }

    private ReconciliationResponse reconcile(DigitalDocument document) {
        List<String> issues = new ArrayList<>();
        boolean originalHashValid = storageService.hashMatches(document.getOriginalFilePath(), document.getOriginalHash());
        boolean signedHashValid = storageService.hashMatches(document.getSignedFilePath(), document.getSignedHash());

        if (!storageService.exists(document.getSignedFilePath())) {
            issues.add("Signed file is missing");
        } else if (!signedHashValid) {
            issues.add("Signed file hash does not match metadata");
        }
        if (!storageService.exists(document.getOriginalFilePath())) {
            issues.add("Original file is missing");
        } else if (!originalHashValid) {
            issues.add("Original file hash does not match metadata");
        }
        if (documentRepository.countByTransactionIdAndDocumentType(
                document.getTransactionId(), document.getDocumentType()) > 1) {
            issues.add("Duplicate document for transactionId + documentType");
        }
        issues.addAll(snapshotIssues(document));

        ReconciliationStatus status = statusFor(issues);
        document.setReconciliationStatus(status);
        DigitalDocument saved = documentRepository.save(document);
        auditService.record("RECONCILE_DOCUMENT", "system", "DigitalDocument", saved.getId(),
                "Reconciliation status: " + status, "{\"issues\":" + issues.size() + "}");
        return new ReconciliationResponse(
                DocumentResponse.from(saved),
                status,
                issues,
                originalHashValid,
                signedHashValid,
                Instant.now()
        );
    }

    private List<String> snapshotIssues(DigitalDocument document) {
        List<String> issues = new ArrayList<>();
        try {
            JsonNode snapshot = objectMapper.readTree(document.getDataSnapshot());
            compareText(issues, "transactionId", document.getTransactionId(), snapshot.path("businessKey").asText(null));
            compareText(issues, "customerId", document.getCustomerId(), snapshot.path("customerId").asText(null));
            compareText(issues, "documentType", document.getDocumentType(), snapshot.path("recordType").asText(null));
            compareText(issues, "branchCode", document.getBranchCode(), snapshot.path("branchCode").asText(null));
            compareText(issues, "currency", document.getCurrency(), snapshot.path("currency").asText(null));
        } catch (Exception ex) {
            issues.add("Cannot parse signing data snapshot");
        }
        return issues;
    }

    private void compareText(List<String> issues, String field, String metadataValue, String snapshotValue) {
        if (metadataValue != null && snapshotValue != null && !metadataValue.equals(snapshotValue)) {
            issues.add(field + " differs from signing snapshot");
        }
    }

    private ReconciliationStatus statusFor(List<String> issues) {
        if (issues.isEmpty()) {
            return ReconciliationStatus.MATCHED;
        }
        if (issues.stream().anyMatch(issue -> issue.contains("missing"))) {
            return ReconciliationStatus.MISSING_FILE;
        }
        if (issues.stream().anyMatch(issue -> issue.contains("hash"))) {
            return ReconciliationStatus.HASH_MISMATCH;
        }
        if (issues.stream().anyMatch(issue -> issue.contains("Duplicate"))) {
            return ReconciliationStatus.DUPLICATE;
        }
        return ReconciliationStatus.MISMATCHED;
    }
}
