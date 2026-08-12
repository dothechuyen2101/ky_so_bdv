package com.bidv.t24r.signing.record;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SignedRecordRepository extends JpaRepository<SignedRecord, String>, JpaSpecificationExecutor<SignedRecord> {
    Optional<SignedRecord> findByBusinessKey(String businessKey);

    Optional<SignedRecord> findByBusinessKeyAndStatus(String businessKey, SigningStatus status);

    long countByStatus(SigningStatus status);

    @Query("select r.branchCode, count(r) from SignedRecord r group by r.branchCode order by count(r) desc")
    List<Object[]> countByBranch();

    @Query("select r from SignedRecord r order by coalesce(r.signedAt, r.ingestedAt) desc")
    List<SignedRecord> findRecent(Pageable pageable);
}
