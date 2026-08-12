package com.bidv.t24r.signing.record;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

final class SignedRecordSpecifications {
    private SignedRecordSpecifications() {
    }

    static Specification<SignedRecord> byCriteria(SignedRecordSearchCriteria criteria) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.query() != null && !criteria.query().isBlank()) {
                String like = "%" + criteria.query().trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("businessKey")), like),
                        builder.like(builder.lower(root.get("customerName")), like),
                        builder.like(builder.lower(root.get("accountNo")), like)
                ));
            }
            if (criteria.customerId() != null && !criteria.customerId().isBlank()) {
                predicates.add(builder.equal(root.get("customerId"), criteria.customerId().trim()));
            }
            if (criteria.branchCode() != null && !criteria.branchCode().isBlank()) {
                predicates.add(builder.equal(root.get("branchCode"), criteria.branchCode().trim()));
            }
            if (criteria.status() != null) {
                predicates.add(builder.equal(root.get("status"), criteria.status()));
            }
            if (criteria.from() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("signedAt"), criteria.from()));
            }
            if (criteria.to() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("signedAt"), criteria.to()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
