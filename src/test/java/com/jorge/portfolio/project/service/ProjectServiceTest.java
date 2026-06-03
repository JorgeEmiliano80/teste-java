package com.jorge.portfolio.project.service;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.common.exception.ResourceNotFoundException;
import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.enums.MemberAssignment;
import com.jorge.portfolio.member.service.MemberService;
import com.jorge.portfolio.project.dto.ProjectCreateRequest;
import com.jorge.portfolio.project.dto.ProjectResponse;
import com.jorge.portfolio.project.dto.ProjectStatusUpdateRequest;
import com.jorge.portfolio.project.dto.ProjectUpdateRequest;
import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.mapper.ProjectMapper;
import com.jorge.portfolio.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldCreateProjectSuccessfully() {
        Member manager = member(1L, "Manager", MemberAssignment.GERENTE);
        Member employee = member(2L, "Employee", MemberAssignment.FUNCIONARIO);

        ProjectCreateRequest request = new ProjectCreateRequest(
                "  Data   Platform  ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                new BigDecimal("100000.00"),
                "  Important   project  ",
                1L,
                Set.of(2L)
        );

        Project savedProject = project(
                10L,
                "Data Platform",
                manager,
                ProjectStatus.EM_ANALISE
        );

        ProjectResponse expectedResponse = mock(ProjectResponse.class);

        when(memberService.findEntityById(1L)).thenReturn(manager);
        when(memberService.findEntityById(2L)).thenReturn(employee);
        when(projectRepository.countActiveProjectsByMemberId(2L)).thenReturn(0L);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectMapper.toResponse(savedProject)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.create(request);

        assertThat(response).isSameAs(expectedResponse);

        verify(projectRepository).save(any(Project.class));
        verify(projectMapper).toResponse(savedProject);
    }

    @Test
    void shouldRejectCreateProjectWithoutAllocatedMembers() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Data Platform",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                new BigDecimal("100000.00"),
                "Important project",
                1L,
                Set.of()
        );

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O projeto deve ter pelo menos 1 membro alocado.");

        verifyNoInteractions(projectRepository);
        verifyNoInteractions(memberService);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void shouldRejectCreateProjectWhenAllocatedMemberIsNotEmployee() {
        Member manager = member(1L, "Manager", MemberAssignment.GERENTE);
        Member coordinator = member(2L, "Coordinator", MemberAssignment.COORDENADOR);

        ProjectCreateRequest request = new ProjectCreateRequest(
                "Data Platform",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                new BigDecimal("100000.00"),
                "Important project",
                1L,
                Set.of(2L)
        );

        when(memberService.findEntityById(1L)).thenReturn(manager);
        when(memberService.findEntityById(2L)).thenReturn(coordinator);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Apenas membros com atribuição funcionário podem ser alocados.");

        verify(projectRepository, never()).save(any(Project.class));
        verifyNoInteractions(projectMapper);
    }

    @Test
    void shouldRejectCreateProjectWhenMemberAlreadyHasThreeActiveProjects() {
        Member manager = member(1L, "Manager", MemberAssignment.GERENTE);
        Member employee = member(2L, "Employee", MemberAssignment.FUNCIONARIO);

        ProjectCreateRequest request = new ProjectCreateRequest(
                "Data Platform",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                new BigDecimal("100000.00"),
                "Important project",
                1L,
                Set.of(2L)
        );

        when(memberService.findEntityById(1L)).thenReturn(manager);
        when(memberService.findEntityById(2L)).thenReturn(employee);
        when(projectRepository.countActiveProjectsByMemberId(2L)).thenReturn(3L);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O membro já está alocado em 3 projetos ativos.");

        verify(projectRepository, never()).save(any(Project.class));
        verifyNoInteractions(projectMapper);
    }

    @Test
    void shouldFindProjectById() {
        Project project = project(10L, "Data Platform", member(1L, "Manager", MemberAssignment.GERENTE), ProjectStatus.EM_ANALISE);
        ProjectResponse expectedResponse = mock(ProjectResponse.class);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.findById(10L);

        assertThat(response).isSameAs(expectedResponse);

        verify(projectRepository).findById(10L);
        verify(projectMapper).toResponse(project);
    }

    @Test
    void shouldThrowResourceNotFoundWhenProjectDoesNotExist() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Projeto não encontrado.");

        verify(projectRepository).findById(99L);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void shouldUpdateProjectSuccessfully() {
        Member currentManager = member(1L, "Current Manager", MemberAssignment.GERENTE);
        Member newManager = member(2L, "New Manager", MemberAssignment.GERENTE);

        Project project = project(10L, "Old Name", currentManager, ProjectStatus.EM_ANALISE);

        ProjectUpdateRequest request = new ProjectUpdateRequest(
                "New Name",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 5, 1),
                null,
                new BigDecimal("200000.00"),
                "Updated description",
                2L
        );

        ProjectResponse expectedResponse = mock(ProjectResponse.class);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(memberService.findEntityById(2L)).thenReturn(newManager);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.update(10L, request);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getManager()).isSameAs(newManager);

        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
    }

    @Test
    void shouldDeleteProjectWhenStatusAllowsDeletion() {
        Project project = project(10L, "Data Platform", member(1L, "Manager", MemberAssignment.GERENTE), ProjectStatus.EM_ANALISE);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        projectService.delete(10L);

        verify(projectRepository).delete(project);
    }

    @Test
    void shouldRejectDeleteWhenProjectStatusIsStarted() {
        Project project = project(10L, "Data Platform", member(1L, "Manager", MemberAssignment.GERENTE), ProjectStatus.INICIADO);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.delete(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Projetos com status iniciado, em andamento ou encerrado não podem ser excluídos.");

        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void shouldChangeStatusWhenTransitionIsValid() {
        Project project = project(10L, "Data Platform", member(1L, "Manager", MemberAssignment.GERENTE), ProjectStatus.EM_ANALISE);
        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.ANALISE_REALIZADA);
        ProjectResponse expectedResponse = mock(ProjectResponse.class);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.changeStatus(10L, request);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ANALISE_REALIZADA);

        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
    }

    @Test
    void shouldRejectSkippedStatusTransition() {
        Project project = project(10L, "Data Platform", member(1L, "Manager", MemberAssignment.GERENTE), ProjectStatus.EM_ANALISE);
        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.INICIADO);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.changeStatus(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A transição de status deve respeitar a sequência lógica.");

        verify(projectRepository, never()).save(any(Project.class));
        verifyNoInteractions(projectMapper);
    }

    @Test
    void shouldAddMemberSuccessfully() {
        Member manager = member(1L, "Manager", MemberAssignment.GERENTE);
        Member existingEmployee = member(2L, "Existing Employee", MemberAssignment.FUNCIONARIO);
        Member newEmployee = member(3L, "New Employee", MemberAssignment.FUNCIONARIO);

        Project project = project(10L, "Data Platform", manager, ProjectStatus.EM_ANALISE);
        project.addAllocatedMember(existingEmployee);

        ProjectResponse expectedResponse = mock(ProjectResponse.class);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(memberService.findEntityById(3L)).thenReturn(newEmployee);
        when(projectRepository.countActiveProjectsByMemberId(3L)).thenReturn(0L);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.addMember(10L, 3L);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(project.getAllocatedMembers()).contains(newEmployee);

        verify(projectRepository).save(project);
    }

    @Test
    void shouldRejectAddingNonEmployeeMember() {
        Member manager = member(1L, "Manager", MemberAssignment.GERENTE);
        Member existingEmployee = member(2L, "Existing Employee", MemberAssignment.FUNCIONARIO);
        Member coordinator = member(3L, "Coordinator", MemberAssignment.COORDENADOR);

        Project project = project(10L, "Data Platform", manager, ProjectStatus.EM_ANALISE);
        project.addAllocatedMember(existingEmployee);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(memberService.findEntityById(3L)).thenReturn(coordinator);

        assertThatThrownBy(() -> projectService.addMember(10L, 3L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Apenas membros com atribuição funcionário podem ser alocados.");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldRemoveMemberSuccessfully() {
        Member manager = member(1L, "Manager", MemberAssignment.GERENTE);
        Member employeeOne = member(2L, "Employee One", MemberAssignment.FUNCIONARIO);
        Member employeeTwo = member(3L, "Employee Two", MemberAssignment.FUNCIONARIO);

        Project project = project(10L, "Data Platform", manager, ProjectStatus.EM_ANALISE);
        project.addAllocatedMember(employeeOne);
        project.addAllocatedMember(employeeTwo);

        ProjectResponse expectedResponse = mock(ProjectResponse.class);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(memberService.findEntityById(3L)).thenReturn(employeeTwo);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.removeMember(10L, 3L);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(project.getAllocatedMembers()).doesNotContain(employeeTwo);

        verify(projectRepository).save(project);
    }

    @Test
    void shouldRejectRemovingLastAllocatedMember() {
        Member manager = member(1L, "Manager", MemberAssignment.GERENTE);
        Member employee = member(2L, "Employee", MemberAssignment.FUNCIONARIO);

        Project project = project(10L, "Data Platform", manager, ProjectStatus.EM_ANALISE);
        project.addAllocatedMember(employee);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(memberService.findEntityById(2L)).thenReturn(employee);

        assertThatThrownBy(() -> projectService.removeMember(10L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O projeto deve manter pelo menos 1 membro alocado.");

        verify(projectRepository, never()).save(any(Project.class));
    }

    private Member member(Long id, String name, MemberAssignment assignment) {
        Member member = new Member(name, assignment);
        member.setId(id);
        return member;
    }

    private Project project(Long id, String name, Member manager, ProjectStatus status) {
        Project project = new Project(
                name,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                new BigDecimal("100000.00"),
                manager,
                status
        );
        project.setId(id);
        return project;
    }
}