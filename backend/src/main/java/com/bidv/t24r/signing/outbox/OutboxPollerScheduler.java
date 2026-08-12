package com.bidv.t24r.signing.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.outbox.poller.enabled", havingValue = "true")
public class OutboxPollerScheduler {
    private final OutboxEventService outboxEventService;

    public OutboxPollerScheduler(OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poller.fixed-delay-ms:30000}")
    public void poll() {
        outboxEventService.pollOnly(100);
    }
}
