package com.jorge.portfolio.project.dto;

import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.enums.RiskClassification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record ProjectResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate expectedEndDate,
        LocalDate actualEndDate,
        BigDecimal totalBudget,
        String description,
        MemberResponse manager,
        ProjectStatus status,
        RiskClassification riskClassification,
        Set<MemberResponse> allocatedMembers,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
