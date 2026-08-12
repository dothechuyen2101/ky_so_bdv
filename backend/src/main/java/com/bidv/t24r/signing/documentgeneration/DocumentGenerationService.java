package com.bidv.t24r.signing.documentgeneration;

import com.bidv.t24r.signing.download.MinimalPdfBuilder;
import com.bidv.t24r.signing.ingestion.T24rRawRecord;
import com.bidv.t24r.signing.signing.SignedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class DocumentGenerationService {
    private final ObjectMapper objectMapper;

    public DocumentGenerationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GeneratedDocument generate(String documentCode, T24rRawRecord rawRecord, String canonicalPayload) {
        try {
            List<String> lines = List.of(
                    "Document code: " + documentCode,
                    "Transaction ID: " + rawRecord.businessKey(),
                    "Customer: " + rawRecord.customerId() + " - " + rawRecord.customerName(),
                    "Account: " + maskAccount(rawRecord.accountNo()),
                    "Document type: " + rawRecord.recordType(),
                    "Business type: " + rawRecord.productCode(),
                    "Branch: " + rawRecord.branchCode(),
                    "Business date: " + rawRecord.businessDate(),
                    "Payload SHA-256: " + shaPreview(canonicalPayload)
            );
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("documentCode", documentCode);
            envelope.put("transactionId", rawRecord.businessKey());
            envelope.put("customerId", rawRecord.customerId());
            envelope.put("accountNoMasked", maskAccount(rawRecord.accountNo()));
            envelope.put("documentType", rawRecord.recordType());
            envelope.put("businessType", rawRecord.productCode());
            envelope.put("branchCode", rawRecord.branchCode());
            envelope.put("businessDate", rawRecord.businessDate());
            envelope.put("payload", objectMapper.readTree(canonicalPayload));
            byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(envelope);
            byte[] xml = xml(documentCode, rawRecord, canonicalPayload).getBytes(StandardCharsets.UTF_8);
            byte[] pdf = MinimalPdfBuilder.build("T24R Digital Document", lines);
            return new GeneratedDocument(pdf, json, xml, lines);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot generate document " + documentCode, ex);
        }
    }

    public byte[] renderSignedPdf(GeneratedDocument generatedDocument, SignedPayload signedPayload) {
        List<String> lines = new java.util.ArrayList<>(generatedDocument.pdfLines());
        lines.add("Signature algorithm: SHA-256 mock gateway");
        lines.add("Certificate serial: " + signedPayload.certificateSerial());
        lines.add("Signer: " + signedPayload.signer());
        lines.add("Timestamp: " + signedPayload.signedAt());
        lines.add("Signature: " + signedPayload.signatureValue());
        return MinimalPdfBuilder.build("T24R Digitally Signed Document", lines);
    }

    private String xml(String documentCode, T24rRawRecord rawRecord, String canonicalPayload) {
        return """
                <T24RDigitalDocument>
                  <DocumentCode>%s</DocumentCode>
                  <TransactionId>%s</TransactionId>
                  <CustomerId>%s</CustomerId>
                  <AccountNoMasked>%s</AccountNoMasked>
                  <DocumentType>%s</DocumentType>
                  <BusinessType>%s</BusinessType>
                  <BranchCode>%s</BranchCode>
                  <Payload><![CDATA[%s]]></Payload>
                </T24RDigitalDocument>
                """.formatted(
                escape(documentCode),
                escape(rawRecord.businessKey()),
                escape(rawRecord.customerId()),
                escape(maskAccount(rawRecord.accountNo())),
                escape(rawRecord.recordType()),
                escape(rawRecord.productCode()),
                escape(rawRecord.branchCode()),
                canonicalPayload
        );
    }

    private String maskAccount(String accountNo) {
        if (accountNo == null || accountNo.length() <= 6) {
            return accountNo;
        }
        return accountNo.substring(0, 4) + "****" + accountNo.substring(accountNo.length() - 4);
    }

    private String shaPreview(String canonicalPayload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonicalPayload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot calculate payload hash", ex);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
