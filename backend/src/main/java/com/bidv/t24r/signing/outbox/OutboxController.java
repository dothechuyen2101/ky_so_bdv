package com.bidv.t24r.signing.outbox;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/outbox-events")
public class OutboxController {
    private final OutboxEventService outboxEventService;

    public OutboxController(OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    @PostMapping("/run")
    public OutboxRunResponse run(@RequestBody OutboxRunRequest request) {
        return outboxEventService.run(request);
    }

    @PostMapping("/poll")
    public OutboxRunResponse poll(@RequestParam(defaultValue = "100") int limit) {
        return outboxEventService.pollOnly(limit);
    }

    @GetMapping("/summary")
    public OutboxSummaryResponse summary() {
        return outboxEventService.summary();
    }
}
