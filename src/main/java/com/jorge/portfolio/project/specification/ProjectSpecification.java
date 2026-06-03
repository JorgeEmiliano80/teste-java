package com.jorge.portfolio.project.specification;

import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class ProjectSpecification {

    private ProjectSpecification() {
    }

    public static Specification<Project> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Project> hasStatus(ProjectStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Project> hasManagerId(Long managerId) {
        return (root, query, criteriaBuilder) -> managerId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("manager").get("id"), managerId);
    }

    public static Specification<Project> startDateBetween(LocalDate from, LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            if (from != null && to != null) {
                return criteriaBuilder.between(root.get("startDate"), from, to);
            }
            if (from != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), from);
            }
            if (to != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), to);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Project> budgetBetween(BigDecimal min, BigDecimal max) {
        return (root, query, criteriaBuilder) -> {
            if (min != null && max != null) {
                return criteriaBuilder.between(root.get("totalBudget"), min, max);
            }
            if (min != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("totalBudget"), min);
            }
            if (max != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("totalBudget"), max);
            }
            return criteriaBuilder.conjunction();
        };
    }
}
