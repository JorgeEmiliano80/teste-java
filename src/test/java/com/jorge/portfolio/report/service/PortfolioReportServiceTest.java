package com.jorge.portfolio.report.service;

import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.enums.MemberAssignment;
import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.repository.ProjectRepository;
import com.jorge.portfolio.report.dto.PortfolioSummaryResponse;
import com.jorge.portfolio.report.dto.StatusBudgetSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private PortfolioReportService portfolioReportService;

    @Test
    void shouldGeneratePortfolioSummary() {
        Project closedProject = project(ProjectStatus.ENCERRADO);
        closedProject.setActualEndDate(LocalDate.of(2026, 1, 31));

        when(projectRepository.summarizeBudgetByStatus()).thenReturn(List.of(
                new StatusBudgetSummaryResponse(ProjectStatus.EM_ANALISE, 2L, new BigDecimal("300000.00")),
                new StatusBudgetSummaryResponse(ProjectStatus.ENCERRADO, 1L, new BigDecimal("100000.00"))
        ));
        when(projectRepository.findClosedProjectsWithActualEndDate()).thenReturn(List.of(closedProject));
        when(projectRepository.countUniqueAllocatedMembers()).thenReturn(3L);

        PortfolioSummaryResponse response = portfolioReportService.generatePortfolioSummary();

        assertThat(response.statusSummaries()).hasSize(ProjectStatus.values().length);
        assertThat(response.totalBudget()).isEqualByComparingTo("400000.00");
        assertThat(response.averageClosedProjectDurationInDays()).isEqualByComparingTo("30.00");
        assertThat(response.uniqueAllocatedMembers()).isEqualTo(3L);
        assertThat(response.statusSummaries())
                .filteredOn(summary -> summary.status() == ProjectStatus.EM_ANALISE)
                .singleElement()
                .satisfies(summary -> {
                    assertThat(summary.projectCount()).isEqualTo(2L);
                    assertThat(summary.totalBudget()).isEqualByComparingTo("300000.00");
                });
    }

    @Test
    void shouldReturnZeroAverageWhenThereAreNoClosedProjects() {
        when(projectRepository.summarizeBudgetByStatus()).thenReturn(List.of());
        when(projectRepository.findClosedProjectsWithActualEndDate()).thenReturn(List.of());
        when(projectRepository.countUniqueAllocatedMembers()).thenReturn(0L);

        PortfolioSummaryResponse response = portfolioReportService.generatePortfolioSummary();

        assertThat(response.averageClosedProjectDurationInDays()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.totalBudget()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.uniqueAllocatedMembers()).isZero();
    }

    @Test
    void shouldCountUniqueAllocatedMembersOnlyOnce() {
        Member member = member(2L);
        Project firstProject = project(ProjectStatus.EM_ANDAMENTO);
        Project secondProject = project(ProjectStatus.PLANEJADO);
        firstProject.addAllocatedMember(member);
        secondProject.addAllocatedMember(member);

        when(projectRepository.summarizeBudgetByStatus()).thenReturn(List.of(
                new StatusBudgetSummaryResponse(ProjectStatus.EM_ANDAMENTO, 1L, new BigDecimal("100000.00")),
                new StatusBudgetSummaryResponse(ProjectStatus.PLANEJADO, 1L, new BigDecimal("200000.00"))
        ));
        when(projectRepository.findClosedProjectsWithActualEndDate()).thenReturn(List.of());
        when(projectRepository.countUniqueAllocatedMembers()).thenReturn(1L);

        PortfolioSummaryResponse response = portfolioReportService.generatePortfolioSummary();

        assertThat(response.uniqueAllocatedMembers()).isEqualTo(1L);
        assertThat(response.totalBudget()).isEqualByComparingTo("300000.00");
    }

    private Project project(ProjectStatus status) {
        return new Project(
                "Data Platform",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 1),
                new BigDecimal("100000.00"),
                member(1L),
                status
        );
    }

    private Member member(Long id) {
        Member member = new Member("Ana Souza", MemberAssignment.FUNCIONARIO);
        member.setId(id);
        return member;
    }
}
