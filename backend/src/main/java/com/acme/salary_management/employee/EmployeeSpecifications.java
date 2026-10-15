package com.acme.salary_management.employee;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/** Builds the WHERE clause from whichever filters the HR manager filled in. */
final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(criteria.name())) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + criteria.name().toLowerCase() + "%"));
            }
            if (hasText(criteria.country())) {
                predicates.add(cb.equal(root.get("country"), criteria.country()));
            }
            if (hasText(criteria.jobTitle())) {
                predicates.add(cb.equal(root.get("jobTitle"), criteria.jobTitle()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}