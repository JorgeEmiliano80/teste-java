package com.jorge.portfolio.project.dto;

import com.jorge.portfolio.project.enums.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectFilterRequest(
        String name,
        ProjectStatus status,
        Long managerId,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        BigDecimal minBudget,
        BigDecimal maxBudget
) {
}
