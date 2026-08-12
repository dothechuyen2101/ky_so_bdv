package com.bidv.t24r.signing.audit;

import java.time.Instant;

public record AuditLogResponse(
        String id,
        String action,
        String actor,
        String targetType,
        String targetId,
        String message,
        String metadata,
        Instant createdAt
) {
    static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getAction(), log.getActor(), log.getTargetType(), log.getTargetId(),
                log.getMessage(), log.getMetadata(), log.getCreatedAt());
    }
}
