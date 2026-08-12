package com.bidv.t24r.signing.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signed_records")
public class SignedRecord {
    @Id
    private String id;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "business_key", nullable = false, unique = true)
    private String businessKey;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "record_type")
    private String recordType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SigningStatus status;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "payload_hash", nullable = false)
    private String payloadHash;

    @Lob
    @Column(name = "canonical_payload", nullable = false)
    private String canonicalPayload;

    @Lob
    @Column(name = "signature_value")
    private String signatureValue;

    @Column(name = "certificate_serial")
    private String certificateSerial;

    @Column(name = "signer")
    private String signer;

    @Column(name = "signature_timestamp")
    private String signatureTimestamp;

    @Version
    @Column(name = "version_number")
    private Long versionNumber;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (sourceSystem == null) {
            sourceSystem = "T24R";
        }
        if (status == null) {
            status = SigningStatus.PENDING;
        }
        if (ingestedAt == null) {
            ingestedAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public SigningStatus getStatus() {
        return status;
    }

    public void setStatus(SigningStatus status) {
        this.status = status;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(Instant ingestedAt) {
        this.ingestedAt = ingestedAt;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public String getCanonicalPayload() {
        return canonicalPayload;
    }

    public void setCanonicalPayload(String canonicalPayload) {
        this.canonicalPayload = canonicalPayload;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public String getCertificateSerial() {
        return certificateSerial;
    }

    public void setCertificateSerial(String certificateSerial) {
        this.certificateSerial = certificateSerial;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getSignatureTimestamp() {
        return signatureTimestamp;
    }

    public void setSignatureTimestamp(String signatureTimestamp) {
        this.signatureTimestamp = signatureTimestamp;
    }

    public Long getVersionNumber() {
        return versionNumber;
    }
}
