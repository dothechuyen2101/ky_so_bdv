package com.bidv.t24r.signing.reconciliation;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {
    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/documents/{documentId}")
    public ReconciliationResponse reconcile(@PathVariable String documentId) {
        return reconciliationService.reconcile(documentId);
    }

    @PostMapping("/run")
    public List<ReconciliationResponse> reconcilePending(@RequestParam(defaultValue = "100") int limit) {
        return reconciliationService.reconcilePending(limit);
    }
}
