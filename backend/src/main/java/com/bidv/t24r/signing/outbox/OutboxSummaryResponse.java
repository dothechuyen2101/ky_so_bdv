package com.bidv.t24r.signing.outbox;

import java.util.List;
import java.util.Map;

public record OutboxSummaryResponse(
        long totalEvents,
        Map<OutboxStatus, Long> byStatus,
        List<OutboxEventResponse> recentEvents
) {
}
