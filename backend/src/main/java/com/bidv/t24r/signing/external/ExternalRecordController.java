package com.bidv.t24r.signing.external;

import com.bidv.t24r.signing.document.DocumentDetailResponse;
import com.bidv.t24r.signing.document.DocumentQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external/v1/records")
public class ExternalRecordController {
    private final DocumentQueryService documentQueryService;

    public ExternalRecordController(DocumentQueryService documentQueryService) {
        this.documentQueryService = documentQueryService;
    }

    @GetMapping("/{businessKey}")
    public DocumentDetailResponse getByBusinessKey(@PathVariable String businessKey) {
        return DocumentDetailResponse.from(documentQueryService.getSignedByDocumentCodeOrTransactionId(businessKey));
    }
}
