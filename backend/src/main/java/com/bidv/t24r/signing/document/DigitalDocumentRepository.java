package com.bidv.t24r.signing.document;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface DigitalDocumentRepository
        extends JpaRepository<DigitalDocument, String>, JpaSpecificationExecutor<DigitalDocument> {
    Optional<DigitalDocument> findByDocumentCode(String documentCode);

    Optional<DigitalDocument> findFirstByTransactionIdAndDocumentTypeAndStatusOrderByCreatedAtDesc(
            String transactionId,
            String documentType,
            DocumentStatus status
    );

    Optional<DigitalDocument> findFirstByTransactionIdAndStatusOrderByCreatedAtDesc(
            String transactionId,
            DocumentStatus status
    );

    long countByStatus(DocumentStatus status);

    long countByReconciliationStatus(ReconciliationStatus reconciliationStatus);

    long countByTransactionIdAndDocumentType(String transactionId, String documentType);

    List<DigitalDocument> findByReconciliationStatus(ReconciliationStatus reconciliationStatus, Pageable pageable);

    @Query("select coalesce(d.branchCode, 'UNKNOWN'), count(d) from DigitalDocument d group by coalesce(d.branchCode, 'UNKNOWN') order by count(d) desc")
    List<Object[]> countByBranch();

    @Query("select d from DigitalDocument d order by d.createdAt desc")
    List<DigitalDocument> findRecent(Pageable pageable);
}
