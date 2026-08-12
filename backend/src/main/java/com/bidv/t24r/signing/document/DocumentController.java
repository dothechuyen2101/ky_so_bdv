package com.bidv.t24r.signing.document;

import com.bidv.t24r.signing.download.DownloadFormat;
import com.bidv.t24r.signing.download.DownloadService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentQueryService documentQueryService;
    private final DownloadService downloadService;

    public DocumentController(DocumentQueryService documentQueryService, DownloadService downloadService) {
        this.documentQueryService = documentQueryService;
        this.downloadService = downloadService;
    }

    @GetMapping
    public DocumentSearchResponse search(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String documentCode,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String accountNoMasked,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) ReconciliationStatus reconciliationStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant signedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant signedTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String hash,
            @RequestParam(required = false) String signer,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return documentQueryService.search(new DocumentSearchCriteria(
                transactionId,
                documentCode,
                customerId,
                accountNoMasked,
                documentType,
                businessType,
                branchCode,
                status,
                reconciliationStatus,
                startOfDay(fromDate),
                endOfDay(toDate),
                signedFrom,
                signedTo,
                minAmount,
                maxAmount,
                hash,
                signer
        ), page, size);
    }

    @GetMapping("/{id}")
    public DocumentDetailResponse get(@PathVariable String id) {
        return DocumentDetailResponse.from(documentQueryService.getById(id));
    }

    @GetMapping("/compare")
    public DocumentQueryService.CompareResponse compare(@RequestParam String leftId, @RequestParam String rightId) {
        return documentQueryService.compare(leftId, rightId);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable String id,
            @RequestParam(defaultValue = "PDF") DownloadFormat format
    ) {
        return downloadService.download(documentQueryService.getById(id), format);
    }

    private Instant startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant endOfDay(LocalDate date) {
        return date == null ? null : date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();
    }
}
