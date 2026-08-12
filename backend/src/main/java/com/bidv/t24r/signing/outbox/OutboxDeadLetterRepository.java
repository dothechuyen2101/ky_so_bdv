package com.bidv.t24r.signing.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxDeadLetterRepository extends JpaRepository<OutboxDeadLetter, String> {
}
