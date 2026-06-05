package com.jorge.portfolio.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryResponse(
        List<StatusBudgetSummaryResponse> statusSummaries,
        BigDecimal totalBudget,
        BigDecimal averageClosedProjectDurationInDays,
        long uniqueAllocatedMembers
) {
}
