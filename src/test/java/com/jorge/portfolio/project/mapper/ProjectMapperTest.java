package com.jorge.portfolio.project.mapper;

import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.enums.MemberAssignment;
import com.jorge.portfolio.member.mapper.MemberMapper;
import com.jorge.portfolio.project.dto.ProjectResponse;
import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.enums.RiskClassification;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private final MemberMapper memberMapper = new MemberMapper();
    private final ProjectMapper projectMapper = new ProjectMapper(memberMapper);

    @Test
    void shouldMapProjectToResponseWithCalculatedRiskClassification() {
        Member manager = new Member("Carla Mendes", MemberAssignment.GERENTE);
        manager.setId(1L);

        Member allocatedMember = new Member("João Silva", MemberAssignment.FUNCIONARIO);
        allocatedMember.setId(2L);

        Project project = new Project(
                "Portfolio Modernization",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                new BigDecimal("100000.00"),
                manager,
                ProjectStatus.EM_ANALISE
        );
        project.setId(10L);
        project.setDescription("Internal modernization project");
        project.addAllocatedMember(allocatedMember);

        ProjectResponse response = projectMapper.toResponse(project);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Portfolio Modernization");
        assertThat(response.description()).isEqualTo("Internal modernization project");
        assertThat(response.manager()).isNotNull();
        assertThat(response.manager().id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(ProjectStatus.EM_ANALISE);
        assertThat(response.riskClassification()).isEqualTo(RiskClassification.BAIXO);
        assertThat(response.allocatedMembers()).hasSize(1);
        assertThat(response.allocatedMembers())
                .extracting("id")
                .containsExactly(2L);
    }

    @Test
    void shouldMapProjectAsHighRiskWhenBudgetIsGreaterThanFiveHundredThousand() {
        Member manager = new Member("Carla Mendes", MemberAssignment.GERENTE);
        manager.setId(1L);

        Project project = new Project(
                "Large Data Platform",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1),
                new BigDecimal("500000.01"),
                manager,
                ProjectStatus.EM_ANALISE
        );
        project.setDescription("High budget project");

        ProjectResponse response = projectMapper.toResponse(project);

        assertThat(response.riskClassification()).isEqualTo(RiskClassification.ALTO);
        assertThat(response.description()).isEqualTo("High budget project");
    }

    @Test
    void shouldReturnNullWhenProjectIsNull() {
        ProjectResponse response = projectMapper.toResponse(null);

        assertThat(response).isNull();
    }
}