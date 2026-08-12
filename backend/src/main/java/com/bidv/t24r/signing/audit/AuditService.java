package com.bidv.t24r.signing.audit;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String actor, String targetType, String targetId, String message, String metadata) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActor(actor == null || actor.isBlank() ? "system" : actor);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setMessage(message);
        log.setMetadata(metadata);
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public AuditPage list(int page, int size) {
        var result = repository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), Math.min(size, 100)));
        return new AuditPage(
                result.stream().map(AuditLogResponse::from).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    public record AuditPage(
            java.util.List<AuditLogResponse> items,
            long totalElements,
            int totalPages,
            int page,
            int size
    ) {
    }
}
