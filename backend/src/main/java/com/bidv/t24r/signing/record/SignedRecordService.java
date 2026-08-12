package com.bidv.t24r.signing.record;

import com.bidv.t24r.signing.audit.AuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignedRecordService {
    private final SignedRecordRepository repository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public SignedRecordService(SignedRecordRepository repository, ObjectMapper objectMapper, AuditService auditService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public SignedRecordSearchResponse search(SignedRecordSearchCriteria criteria, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "signedAt").and(Sort.by(Sort.Direction.DESC, "ingestedAt")));
        var result = repository.findAll(SignedRecordSpecifications.byCriteria(criteria), pageable);
        return new SignedRecordSearchResponse(
                result.stream().map(SignedRecordResponse::from).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Transactional(readOnly = true)
    public SignedRecord getById(String id) {
        return repository.findById(id).orElseThrow(() -> new SignedRecordNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public SignedRecord getSignedByBusinessKey(String businessKey) {
        return repository.findByBusinessKeyAndStatus(businessKey, SigningStatus.SIGNED)
                .orElseThrow(() -> new SignedRecordNotFoundException(businessKey));
    }

    @Transactional(readOnly = true)
    public CompareResponse compare(String leftId, String rightId) {
        SignedRecord left = getById(leftId);
        SignedRecord right = getById(rightId);
        try {
            JsonNode leftNode = objectMapper.readTree(left.getCanonicalPayload());
            JsonNode rightNode = objectMapper.readTree(right.getCanonicalPayload());
            List<FieldDifference> differences = new ArrayList<>();
            compareNode("", leftNode, rightNode, differences);
            auditService.record("COMPARE_RECORDS", "system", "SignedRecord", leftId + "," + rightId,
                    "Compared signed payloads", null);
            return new CompareResponse(
                    SignedRecordResponse.from(left),
                    SignedRecordResponse.from(right),
                    differences
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot compare signed payloads", ex);
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
            SignedRecordResponse left,
            SignedRecordResponse right,
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
