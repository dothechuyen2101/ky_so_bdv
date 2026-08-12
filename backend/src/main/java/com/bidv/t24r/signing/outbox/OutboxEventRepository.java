package com.bidv.t24r.signing.outbox;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    Optional<OutboxEvent> findByEventId(String eventId);

    long countByStatus(OutboxStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEvent e where e.status in :statuses order by e.createdAt asc")
    List<OutboxEvent> findNextForUpdate(@Param("statuses") Collection<OutboxStatus> statuses, Pageable pageable);

    @Query("select e from OutboxEvent e order by e.createdAt desc")
    List<OutboxEvent> findRecent(Pageable pageable);
}
