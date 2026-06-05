package com.jorge.portfolio.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.common.exception.GlobalExceptionHandler;
import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.member.enums.MemberAssignment;
import com.jorge.portfolio.project.dto.ProjectCreateRequest;
import com.jorge.portfolio.project.dto.ProjectResponse;
import com.jorge.portfolio.project.dto.ProjectStatusUpdateRequest;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.enums.RiskClassification;
import com.jorge.portfolio.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerTest {

    private final ProjectService projectService = mock(ProjectService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProjectController(projectService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateProject() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Data Platform",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                new BigDecimal("100000.00"),
                "Important project",
                1L,
                Set.of(2L)
        );

        ProjectResponse response = projectResponse();

        when(projectService.create(any(ProjectCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Data Platform"))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.riskClassification").value("BAIXO"));

        verify(projectService).create(any(ProjectCreateRequest.class));
    }

    @Test
    void shouldFindProjectById() throws Exception {
        when(projectService.findById(10L)).thenReturn(projectResponse());

        mockMvc.perform(get("/api/projects/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Data Platform"));

        verify(projectService).findById(10L);
    }

    @Test
    void shouldListProjectsWithFiltersAndPagination() throws Exception {
        when(projectService.findAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of(projectResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/projects")
                        .param("name", "Data")
                        .param("status", "EM_ANALISE")
                        .param("managerId", "1")
                        .param("startDateFrom", "2026-01-01")
                        .param("startDateTo", "2026-12-31")
                        .param("minBudget", "1000")
                        .param("maxBudget", "500000")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(10L));

        verify(projectService).findAll(any(), any());
    }

    @Test
    void shouldChangeProjectStatus() throws Exception {
        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.ANALISE_REALIZADA);

        ProjectResponse response = projectResponse(ProjectStatus.ANALISE_REALIZADA);

        when(projectService.changeStatus(eq(10L), any(ProjectStatusUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/projects/{id}/status", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANALISE_REALIZADA"));

        verify(projectService).changeStatus(eq(10L), any(ProjectStatusUpdateRequest.class));
    }

    @Test
    void shouldDeleteProject() throws Exception {
        doNothing().when(projectService).delete(10L);

        mockMvc.perform(delete("/api/projects/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(projectService).delete(10L);
    }

    @Test
    void shouldAddMemberToProject() throws Exception {
        when(projectService.addMember(10L, 2L)).thenReturn(projectResponse());

        mockMvc.perform(post("/api/projects/{projectId}/members/{memberId}", 10L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(projectService).addMember(10L, 2L);
    }

    @Test
    void shouldRemoveMemberFromProject() throws Exception {
        when(projectService.removeMember(10L, 2L)).thenReturn(projectResponse());

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", 10L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(projectService).removeMember(10L, 2L);
    }

    @Test
    void shouldReturnBadRequestWhenBusinessExceptionIsThrown() throws Exception {
        when(projectService.findById(10L))
                .thenThrow(new BusinessException("Regra de negócio violada."));

        mockMvc.perform(get("/api/projects/{id}", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Regra de negócio violada."))
                .andExpect(jsonPath("$.path").value("/api/projects/10"));
    }

    private ProjectResponse projectResponse() {
        return projectResponse(ProjectStatus.EM_ANALISE);
    }

    private ProjectResponse projectResponse(ProjectStatus status) {
        MemberResponse manager = new MemberResponse(
                1L,
                "Carla Mendes",
                MemberAssignment.GERENTE,
                null,
                null
        );

        MemberResponse allocatedMember = new MemberResponse(
                2L,
                "João Silva",
                MemberAssignment.FUNCIONARIO,
                null,
                null
        );

        return new ProjectResponse(
                10L,
                "Data Platform",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                null,
                new BigDecimal("100000.00"),
                "Important project",
                manager,
                status,
                RiskClassification.BAIXO,
                Set.of(allocatedMember),
                null,
                null
        );
    }
}
