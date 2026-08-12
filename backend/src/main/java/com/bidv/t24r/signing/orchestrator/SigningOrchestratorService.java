package com.bidv.t24r.signing.orchestrator;

import com.bidv.t24r.signing.audit.AuditService;
import com.bidv.t24r.signing.document.DigitalDocument;
import com.bidv.t24r.signing.document.DigitalDocumentRepository;
import com.bidv.t24r.signing.document.DocumentStatus;
import com.bidv.t24r.signing.document.ReconciliationStatus;
import com.bidv.t24r.signing.documentgeneration.DocumentGenerationService;
import com.bidv.t24r.signing.documentgeneration.GeneratedDocument;
import com.bidv.t24r.signing.ingestion.CanonicalPayloadService;
import com.bidv.t24r.signing.ingestion.T24rRawRecord;
import com.bidv.t24r.signing.outbox.OutboxEvent;
import com.bidv.t24r.signing.signing.SignedPayload;
import com.bidv.t24r.signing.signing.SigningGateway;
import com.bidv.t24r.signing.storage.LocalFileStorageService;
import com.bidv.t24r.signing.storage.StoredFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

@Service
public class SigningOrchestratorService {
    private final ObjectMapper objectMapper;
    private final CanonicalPayloadService canonicalPayloadService;
    private final DocumentGenerationService documentGenerationService;
    private final SigningGateway signingGateway;
    private final LocalFileStorageService storageService;
    private final DigitalDocumentRepository documentRepository;
    private final AuditService auditService;

    public SigningOrchestratorService(
            ObjectMapper objectMapper,
            CanonicalPayloadService canonicalPayloadService,
            DocumentGenerationService documentGenerationService,
            SigningGateway signingGateway,
            LocalFileStorageService storageService,
            DigitalDocumentRepository documentRepository,
            AuditService auditService
    ) {
        this.objectMapper = objectMapper;
        this.canonicalPayloadService = canonicalPayloadService;
        this.documentGenerationService = documentGenerationService;
        this.signingGateway = signingGateway;
        this.storageService = storageService;
        this.documentRepository = documentRepository;
        this.auditService = auditService;
    }

    public DigitalDocument process(OutboxEvent event) {
        T24rRawRecord rawRecord = parse(event);
        validate(rawRecord);
        return documentRepository.findFirstByTransactionIdAndDocumentTypeAndStatusOrderByCreatedAtDesc(
                        rawRecord.businessKey(), rawRecord.recordType(), DocumentStatus.SIGNED)
                .map(existing -> {
                    auditService.record("ORCHESTRATOR_IDEMPOTENT", "system", "DigitalDocument", existing.getId(),
                            "Skipped duplicate signing request " + rawRecord.businessKey(), null);
                    return existing;
                })
                .orElseGet(() -> signAndStore(rawRecord));
    }

    private DigitalDocument signAndStore(T24rRawRecord rawRecord) {
        String documentCode = "DOC-" + rawRecord.businessKey();
        int version = 1;
        Instant transactionDate = transactionDate(rawRecord.businessDate());
        String canonicalPayload = canonicalPayloadService.canonicalize(rawRecord);
        GeneratedDocument generatedDocument = documentGenerationService.generate(documentCode, rawRecord, canonicalPayload);
        SignedPayload signedPayload = signingGateway.sign(documentCode, canonicalPayload);
        byte[] signedPdf = documentGenerationService.renderSignedPdf(generatedDocument, signedPayload);

        StoredFile original = storageService.store(
                "original",
                transactionDate,
                rawRecord.recordType(),
                documentCode + "_v" + version + "_original.pdf",
                generatedDocument.pdfBytes(),
                "application/pdf"
        );
        StoredFile signed = storageService.store(
                "signed",
                transactionDate,
                rawRecord.recordType(),
                documentCode + "_v" + version + "_signed.pdf",
                signedPdf,
                "application/pdf"
        );

        DigitalDocument document = new DigitalDocument();
        document.setDocumentCode(documentCode);
        document.setSourceSystem("T24R");
        document.setTransactionId(rawRecord.businessKey());
        document.setCustomerId(rawRecord.customerId());
        document.setCustomerName(rawRecord.customerName());
        document.setAccountNoMasked(maskAccount(rawRecord.accountNo()));
        document.setDocumentType(rawRecord.recordType());
        document.setBusinessType(rawRecord.productCode());
        document.setBranchCode(rawRecord.branchCode());
        document.setAmount(amount(rawRecord));
        document.setCurrency(textPayload(rawRecord, "currency"));
        document.setTransactionDate(transactionDate);
        document.setSignedAt(signedPayload.signedAt());
        document.setStatus(DocumentStatus.SIGNED);
        document.setReconciliationStatus(ReconciliationStatus.NOT_CHECKED);
        document.setVersionNumber(version);
        document.setOriginalFilePath(original.relativePath());
        document.setSignedFilePath(signed.relativePath());
        document.setOriginalHash(original.sha256());
        document.setSignedHash(signed.sha256());
        document.setFileSize(signed.size());
        document.setMimeType(signed.mimeType());
        document.setSignatureAlgorithm("SHA256-MOCK-HSM-TSA");
        document.setCertificateSerial(signedPayload.certificateSerial());
        document.setSignerSubject(signedPayload.signer());
        document.setTimestampTokenId("MOCK-TSA-" + signedPayload.signedAt().toEpochMilli());
        document.setDataSnapshot(canonicalPayload);
        document.setSignatureValue(signedPayload.signatureValue());

        DigitalDocument persisted = documentRepository.save(document);
        auditService.record("SIGN_DOCUMENT", "system", "DigitalDocument", persisted.getId(),
                "Generated, signed and stored " + documentCode,
                "{\"documentCode\":\"" + documentCode + "\",\"signedPath\":\"" + signed.relativePath() + "\"}");
        return persisted;
    }

    private T24rRawRecord parse(OutboxEvent event) {
        try {
            return objectMapper.readValue(event.getPayload(), T24rRawRecord.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot parse outbox payload " + event.getEventId(), ex);
        }
    }

    private void validate(T24rRawRecord rawRecord) {
        if (blank(rawRecord.businessKey()) || blank(rawRecord.customerId()) || blank(rawRecord.recordType())) {
            throw new IllegalStateException("Outbox payload is missing required signing metadata");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private Instant transactionDate(LocalDate businessDate) {
        LocalDate date = businessDate == null ? LocalDate.now(ZoneOffset.UTC) : businessDate;
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private String maskAccount(String accountNo) {
        if (accountNo == null || accountNo.length() <= 6) {
            return accountNo;
        }
        return accountNo.substring(0, 4) + "****" + accountNo.substring(accountNo.length() - 4);
    }

    private BigDecimal amount(T24rRawRecord rawRecord) {
        Object value = rawRecord.payload() == null ? null : rawRecord.payload().get("balance");
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return new BigDecimal(value.toString());
    }

    private String textPayload(T24rRawRecord rawRecord, String key) {
        Object value = rawRecord.payload() == null ? null : rawRecord.payload().get(key);
        return value == null ? null : value.toString();
    }
}
