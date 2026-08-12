package com.bidv.t24r.signing.document;

import com.bidv.t24r.signing.audit.AuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentQueryService {
    private final DigitalDocumentRepository repository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public DocumentQueryService(
            DigitalDocumentRepository repository,
            ObjectMapper objectMapper,
            AuditService auditService
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public DocumentSearchResponse search(DocumentSearchCriteria criteria, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "signedAt").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        var result = repository.findAll(DigitalDocumentSpecifications.byCriteria(criteria), pageable);
        return new DocumentSearchResponse(
                result.stream().map(DocumentResponse::from).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Transactional(readOnly = true)
    public DigitalDocument getById(String id) {
        return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public DigitalDocument getSignedByDocumentCodeOrTransactionId(String key) {
        return repository.findByDocumentCode(key)
                .or(() -> repository.findFirstByTransactionIdAndStatusOrderByCreatedAtDesc(key, DocumentStatus.SIGNED))
                .orElseThrow(() -> new DocumentNotFoundException(key));
    }

    @Transactional(readOnly = true)
    public CompareResponse compare(String leftId, String rightId) {
        DigitalDocument left = getById(leftId);
        DigitalDocument right = getById(rightId);
        try {
            JsonNode leftNode = objectMapper.readTree(left.getDataSnapshot());
            JsonNode rightNode = objectMapper.readTree(right.getDataSnapshot());
            List<FieldDifference> differences = new ArrayList<>();
            compareNode("", leftNode, rightNode, differences);
            auditService.record("COMPARE_DOCUMENTS", "system", "DigitalDocument", leftId + "," + rightId,
                    "Compared signed data snapshots", null);
            return new CompareResponse(DocumentResponse.from(left), DocumentResponse.from(right), differences);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot compare document snapshots", ex);
        }
    }

    private void compareNode(String path, JsonNode left, JsonNode right, List<FieldDifference> differences) {
        if (left == null && right == null) {
            return;
        }
        if (left == null || right == null || left.getNodeType() != right.getNodeType()) {
            differences.add(FieldDifference.of(path, left, right));
            return;
        }
        if (left.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = left.fields();
            List<String> visited = new ArrayList<>();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                visited.add(field.getKey());
                compareNode(path + "/" + field.getKey(), field.getValue(), right.get(field.getKey()), differences);
            }
            right.fieldNames().forEachRemaining(name -> {
                if (!visited.contains(name)) {
                    compareNode(path + "/" + name, null, right.get(name), differences);
                }
            });
            return;
        }
        if (left.isArray()) {
            int max = Math.max(left.size(), right.size());
            for (int index = 0; index < max; index++) {
                compareNode(path + "/" + index, left.get(index), right.get(index), differences);
            }
            return;
        }
        if (!left.equals(right)) {
            differences.add(FieldDifference.of(path, left, right));
        }
    }

    public record CompareResponse(
            DocumentResponse left,
            DocumentResponse right,
            List<FieldDifference> differences
    ) {
    }

    public record FieldDifference(String path, String leftValue, String rightValue) {
        static FieldDifference of(String path, JsonNode left, JsonNode right) {
            return new FieldDifference(path.isBlank() ? "/" : path, valueOf(left), valueOf(right));
        }

        private static String valueOf(JsonNode node) {
            return node == null ? null : node.toString();
        }
    }
}
