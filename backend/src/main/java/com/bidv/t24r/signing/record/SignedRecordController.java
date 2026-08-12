package com.bidv.t24r.signing.record;

import com.bidv.t24r.signing.download.DownloadFormat;
import com.bidv.t24r.signing.download.DownloadService;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signed-records")
public class SignedRecordController {
    private final SignedRecordService signedRecordService;
    private final DownloadService downloadService;

    public SignedRecordController(SignedRecordService signedRecordService, DownloadService downloadService) {
        this.signedRecordService = signedRecordService;
        this.downloadService = downloadService;
    }

    @GetMapping
    public SignedRecordSearchResponse search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) SigningStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return signedRecordService.search(new SignedRecordSearchCriteria(query, customerId, branchCode, status, from, to),
                page, size);
    }

    @GetMapping("/{id}")
    public SignedRecordDetailResponse get(@PathVariable String id) {
        return SignedRecordDetailResponse.from(signedRecordService.getById(id));
    }

    @GetMapping("/compare")
    public SignedRecordService.CompareResponse compare(@RequestParam String leftId, @RequestParam String rightId) {
        return signedRecordService.compare(leftId, rightId);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable String id,
            @RequestParam(defaultValue = "JSON") DownloadFormat format
    ) {
        return downloadService.download(signedRecordService.getById(id), format);
    }
}
