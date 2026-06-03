package com.jorge.portfolio.project.repository;

import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    long countByStatus(ProjectStatus status);

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
