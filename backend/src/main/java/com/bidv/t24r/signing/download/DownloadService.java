package com.bidv.t24r.signing.download;

import com.bidv.t24r.signing.audit.AuditService;
import com.bidv.t24r.signing.document.DigitalDocument;
import com.bidv.t24r.signing.record.SignedRecord;
import com.bidv.t24r.signing.storage.LocalFileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {
    private final ObjectMapper objectMapper;
    private final LocalFileStorageService storageService;
    private final AuditService auditService;

    public DownloadService(ObjectMapper objectMapper, LocalFileStorageService storageService, AuditService auditService) {
        this.objectMapper = objectMapper;
        this.storageService = storageService;
        this.auditService = auditService;
    }

    public ResponseEntity<byte[]> download(DigitalDocument document, DownloadFormat format) {
        byte[] signedPdf = storageService.readVerified(document.getSignedFilePath(), document.getSignedHash());
        byte[] bytes = switch (format) {
            case JSON -> json(document);
            case XML -> xml(document).getBytes(StandardCharsets.UTF_8);
            case CSV -> csv(document).getBytes(StandardCharsets.UTF_8);
            case PDF -> signedPdf;
        };
        auditService.record("DOWNLOAD_DOCUMENT", "system", "DigitalDocument", document.getId(),
                "Downloaded signed document as " + format, "{\"signedPath\":\"" + document.getSignedFilePath() + "\"}");
        return ResponseEntity.ok()
                .contentType(mediaType(format))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(document.getDocumentCode() + "." + extension(format))
                        .build()
                        .toString())
                .body(bytes);
    }

    public ResponseEntity<byte[]> download(SignedRecord record, DownloadFormat format) {
        byte[] bytes = switch (format) {
            case JSON -> json(record);
            case XML -> xml(record).getBytes(StandardCharsets.UTF_8);
            case CSV -> csv(record).getBytes(StandardCharsets.UTF_8);
            case PDF -> MinimalPdfBuilder.build(record);
        };
        auditService.record("DOWNLOAD_RECORD", "system", "SignedRecord", record.getId(),
                "Downloaded signed record as " + format, null);
        return ResponseEntity.ok()
                .contentType(mediaType(format))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(record.getBusinessKey() + "." + extension(format))
                        .build()
                        .toString())
                .body(bytes);
    }

    private byte[] json(DigitalDocument document) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("documentCode", document.getDocumentCode());
            envelope.put("transactionId", document.getTransactionId());
            envelope.put("sourceSystem", document.getSourceSystem());
            envelope.put("customerId", document.getCustomerId());
            envelope.put("accountNoMasked", document.getAccountNoMasked());
            envelope.put("documentType", document.getDocumentType());
            envelope.put("businessType", document.getBusinessType());
            envelope.put("branchCode", document.getBranchCode());
            envelope.put("amount", document.getAmount());
            envelope.put("currency", document.getCurrency());
            envelope.put("status", document.getStatus());
            envelope.put("reconciliationStatus", document.getReconciliationStatus());
            envelope.put("originalHash", document.getOriginalHash());
            envelope.put("signedHash", document.getSignedHash());
            envelope.put("signedFilePath", document.getSignedFilePath());
            envelope.put("certificateSerial", document.getCertificateSerial());
            envelope.put("signerSubject", document.getSignerSubject());
            envelope.put("timestampTokenId", document.getTimestampTokenId());
            envelope.put("signedAt", document.getSignedAt());
            envelope.put("dataSnapshot", objectMapper.readTree(document.getDataSnapshot()));
            envelope.put("signatureValue", document.getSignatureValue());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot export document JSON", ex);
        }
    }

    private String xml(DigitalDocument document) {
        return """
                <DigitalDocument>
                  <DocumentCode>%s</DocumentCode>
                  <TransactionId>%s</TransactionId>
                  <CustomerId>%s</CustomerId>
                  <AccountNoMasked>%s</AccountNoMasked>
                  <DocumentType>%s</DocumentType>
                  <BusinessType>%s</BusinessType>
                  <BranchCode>%s</BranchCode>
                  <Status>%s</Status>
                  <ReconciliationStatus>%s</ReconciliationStatus>
                  <SignedHash>%s</SignedHash>
                  <CertificateSerial>%s</CertificateSerial>
                  <SignerSubject>%s</SignerSubject>
                  <SignedAt>%s</SignedAt>
                  <DataSnapshot><![CDATA[%s]]></DataSnapshot>
                </DigitalDocument>
                """.formatted(
                escape(document.getDocumentCode()),
                escape(document.getTransactionId()),
                escape(document.getCustomerId()),
                escape(document.getAccountNoMasked()),
                escape(document.getDocumentType()),
                escape(document.getBusinessType()),
                escape(document.getBranchCode()),
                document.getStatus(),
                document.getReconciliationStatus(),
                escape(document.getSignedHash()),
                escape(document.getCertificateSerial()),
                escape(document.getSignerSubject()),
                document.getSignedAt(),
                document.getDataSnapshot()
        );
    }

    private String csv(DigitalDocument document) {
        return "documentCode,transactionId,customerId,accountNoMasked,documentType,branchCode,status,reconciliationStatus,signedHash,signedAt\n"
                + escapeCsv(document.getDocumentCode()) + ","
                + escapeCsv(document.getTransactionId()) + ","
                + escapeCsv(document.getCustomerId()) + ","
                + escapeCsv(document.getAccountNoMasked()) + ","
                + escapeCsv(document.getDocumentType()) + ","
                + escapeCsv(document.getBranchCode()) + ","
                + document.getStatus() + ","
                + document.getReconciliationStatus() + ","
                + escapeCsv(document.getSignedHash()) + ","
                + document.getSignedAt() + "\n";
    }

    private byte[] json(SignedRecord record) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("businessKey", record.getBusinessKey());
            envelope.put("payloadHash", record.getPayloadHash());
            envelope.put("signatureValue", record.getSignatureValue());
            envelope.put("certificateSerial", record.getCertificateSerial());
            envelope.put("signer", record.getSigner());
            envelope.put("signedAt", record.getSignedAt());
            envelope.put("payload", objectMapper.readTree(record.getCanonicalPayload()));
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot export JSON", ex);
        }
    }

    private String xml(SignedRecord record) {
        return """
                <SignedT24RRecord>
                  <BusinessKey>%s</BusinessKey>
                  <PayloadHash>%s</PayloadHash>
                  <CertificateSerial>%s</CertificateSerial>
                  <Signer>%s</Signer>
                  <SignedAt>%s</SignedAt>
                  <SignatureValue>%s</SignatureValue>
                  <CanonicalPayload><![CDATA[%s]]></CanonicalPayload>
                </SignedT24RRecord>
                """.formatted(
                escape(record.getBusinessKey()),
                escape(record.getPayloadHash()),
                escape(record.getCertificateSerial()),
                escape(record.getSigner()),
                record.getSignedAt(),
                escape(record.getSignatureValue()),
                record.getCanonicalPayload()
        );
    }

    private String csv(SignedRecord record) {
        return "businessKey,customerId,accountNo,status,payloadHash,certificateSerial,signedAt\n"
                + escapeCsv(record.getBusinessKey()) + ","
                + escapeCsv(record.getCustomerId()) + ","
                + escapeCsv(record.getAccountNo()) + ","
                + record.getStatus() + ","
                + escapeCsv(record.getPayloadHash()) + ","
                + escapeCsv(record.getCertificateSerial()) + ","
                + record.getSignedAt() + "\n";
    }

    private MediaType mediaType(DownloadFormat format) {
        return switch (format) {
            case JSON -> MediaType.APPLICATION_JSON;
            case XML -> MediaType.APPLICATION_XML;
            case CSV -> MediaType.parseMediaType("text/csv");
            case PDF -> MediaType.APPLICATION_PDF;
        };
    }

    private String extension(DownloadFormat format) {
        return switch (format) {
            case JSON -> "json";
            case XML -> "xml";
            case CSV -> "csv";
            case PDF -> "pdf";
        };
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
