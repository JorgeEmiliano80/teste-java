package com.jorge.portfolio.report.dto;

import com.jorge.portfolio.project.enums.ProjectStatus;
import java.math.BigDecimal;

public record StatusBudgetSummaryResponse(
        ProjectStatus status,
        long projectCount,
        BigDecimal totalBudget
) {
}
