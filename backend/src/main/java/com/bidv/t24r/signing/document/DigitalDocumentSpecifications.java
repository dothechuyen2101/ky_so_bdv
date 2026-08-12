package com.bidv.t24r.signing.document;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

final class DigitalDocumentSpecifications {
    private DigitalDocumentSpecifications() {
    }

    static Specification<DigitalDocument> byCriteria(DocumentSearchCriteria criteria) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            containsIfPresent(predicates, builder, root.get("transactionId"), criteria.transactionId());
            containsIfPresent(predicates, builder, root.get("documentCode"), criteria.documentCode());
            containsIfPresent(predicates, builder, root.get("customerId"), criteria.customerId());
            containsIfPresent(predicates, builder, root.get("accountNoMasked"), criteria.accountNoMasked());
            containsIfPresent(predicates, builder, root.get("documentType"), criteria.documentType());
            containsIfPresent(predicates, builder, root.get("businessType"), criteria.businessType());
            containsIfPresent(predicates, builder, root.get("branchCode"), criteria.branchCode());
            if (criteria.status() != null) {
                predicates.add(builder.equal(root.get("status"), criteria.status()));
            }
            if (criteria.reconciliationStatus() != null) {
                predicates.add(builder.equal(root.get("reconciliationStatus"), criteria.reconciliationStatus()));
            }
            if (criteria.transactionFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("transactionDate"), criteria.transactionFrom()));
            }
            if (criteria.transactionTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("transactionDate"), criteria.transactionTo()));
            }
            if (criteria.signedFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("signedAt"), criteria.signedFrom()));
            }
            if (criteria.signedTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("signedAt"), criteria.signedTo()));
            }
            if (criteria.minAmount() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("amount"), criteria.minAmount()));
            }
            if (criteria.maxAmount() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("amount"), criteria.maxAmount()));
            }
            if (criteria.hash() != null && !criteria.hash().isBlank()) {
                String hash = containsPattern(criteria.hash());
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("originalHash")), hash, '\\'),
                        builder.like(builder.lower(root.get("signedHash")), hash, '\\')
                ));
            }
            containsIfPresent(predicates, builder, root.get("signerSubject"), criteria.signer());
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void containsIfPresent(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            jakarta.persistence.criteria.Path<String> path,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            predicates.add(builder.like(builder.lower(path), containsPattern(value), '\\'));
        }
    }

    private static String containsPattern(String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
