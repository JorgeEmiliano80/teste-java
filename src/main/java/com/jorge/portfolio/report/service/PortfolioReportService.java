package com.jorge.portfolio.report.service;

import com.jorge.portfolio.project.domain.ProjectDurationCalculator;
import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.repository.ProjectRepository;
import com.jorge.portfolio.report.dto.PortfolioSummaryResponse;
import com.jorge.portfolio.report.dto.StatusBudgetSummaryResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioReportService {

    private static final int AVERAGE_SCALE = 2;

    private final ProjectRepository projectRepository;

    public PortfolioReportService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse generatePortfolioSummary() {
        List<StatusBudgetSummaryResponse> statusSummaries = buildStatusSummaries();
        BigDecimal totalBudget = statusSummaries.stream()
                .map(StatusBudgetSummaryResponse::totalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioSummaryResponse(
                statusSummaries,
                totalBudget,
                calculateAverageClosedProjectDurationInDays(),
                projectRepository.countUniqueAllocatedMembers()
        );
    }

    private List<StatusBudgetSummaryResponse> buildStatusSummaries() {
        Map<ProjectStatus, StatusBudgetSummaryResponse> summariesByStatus = new EnumMap<>(ProjectStatus.class);

        projectRepository.summarizeBudgetByStatus()
                .forEach(summary -> summariesByStatus.put(summary.status(), normalizeSummary(summary)));

        return Arrays.stream(ProjectStatus.values())
                .map(status -> summariesByStatus.getOrDefault(
                        status,
                        new StatusBudgetSummaryResponse(status, 0L, BigDecimal.ZERO)
                ))
                .toList();
    }

    private StatusBudgetSummaryResponse normalizeSummary(StatusBudgetSummaryResponse summary) {
        BigDecimal totalBudget = summary.totalBudget() == null
                ? BigDecimal.ZERO
                : summary.totalBudget();

        return new StatusBudgetSummaryResponse(summary.status(), summary.projectCount(), totalBudget);
    }

    private BigDecimal calculateAverageClosedProjectDurationInDays() {
        List<Project> closedProjects = projectRepository.findClosedProjectsWithActualEndDate();

        if (closedProjects.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long totalDays = closedProjects.stream()
                .mapToLong(project -> ProjectDurationCalculator.daysBetween(
                        project.getStartDate(),
                        project.getActualEndDate()
                ))
                .sum();

        return BigDecimal.valueOf(totalDays)
                .divide(BigDecimal.valueOf(closedProjects.size()), AVERAGE_SCALE, RoundingMode.HALF_UP);
    }
}
