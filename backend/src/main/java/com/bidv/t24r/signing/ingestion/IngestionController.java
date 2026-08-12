package com.bidv.t24r.signing.ingestion;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingestions")
public class IngestionController {
    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/run")
    public IngestionRunResponse run(@Valid @RequestBody(required = false) IngestionRequest request) {
        return ingestionService.run(request == null ? new IngestionRequest(null, 20) : request);
    }
}
