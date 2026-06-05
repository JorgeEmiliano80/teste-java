package com.jorge.portfolio.project.repository;

import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.report.dto.StatusBudgetSummaryResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    long countByStatus(ProjectStatus status);

    @Query("""
            SELECT new com.jorge.portfolio.report.dto.StatusBudgetSummaryResponse(
                project.status,
                COUNT(project),
                SUM(project.totalBudget)
            )
            FROM Project project
            GROUP BY project.status
            """)
    List<StatusBudgetSummaryResponse> summarizeBudgetByStatus();

    @Query("""
            SELECT project
            FROM Project project
            WHERE project.status = com.jorge.portfolio.project.enums.ProjectStatus.ENCERRADO
              AND project.actualEndDate IS NOT NULL
            """)
    List<Project> findClosedProjectsWithActualEndDate();

    @Query("""
            SELECT COUNT(DISTINCT member.id)
            FROM Project project
            JOIN project.allocatedMembers member
            """)
    long countUniqueAllocatedMembers();

    @Query("""
            SELECT COUNT(project)
            FROM Project project
            JOIN project.allocatedMembers member
            WHERE member.id = :memberId
              AND project.status NOT IN (
                  com.jorge.portfolio.project.enums.ProjectStatus.ENCERRADO,
                  com.jorge.portfolio.project.enums.ProjectStatus.CANCELADO
              )
            """)
    long countActiveProjectsByMemberId(@Param("memberId") Long memberId);
}
