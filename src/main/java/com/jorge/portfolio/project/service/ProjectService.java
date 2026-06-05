package com.jorge.portfolio.project.service;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.common.exception.ResourceNotFoundException;
import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.enums.MemberAssignment;
import com.jorge.portfolio.member.service.MemberService;
import com.jorge.portfolio.project.domain.ProjectDeletionPolicy;
import com.jorge.portfolio.project.domain.ProjectStatusTransitionPolicy;
import com.jorge.portfolio.project.dto.ProjectCreateRequest;
import com.jorge.portfolio.project.dto.ProjectFilterRequest;
import com.jorge.portfolio.project.dto.ProjectResponse;
import com.jorge.portfolio.project.dto.ProjectStatusUpdateRequest;
import com.jorge.portfolio.project.dto.ProjectUpdateRequest;
import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.mapper.ProjectMapper;
import com.jorge.portfolio.project.repository.ProjectRepository;
import com.jorge.portfolio.project.specification.ProjectSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class ProjectService {

    private static final int MIN_ALLOCATED_MEMBERS = 1;
    private static final int MAX_ALLOCATED_MEMBERS = 10;
    private static final int MAX_ACTIVE_PROJECTS_PER_MEMBER = 3;

    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final ProjectMapper projectMapper;

    public ProjectService(
            ProjectRepository projectRepository,
            MemberService memberService,
            ProjectMapper projectMapper
    ) {
        this.projectRepository = projectRepository;
        this.memberService = memberService;
        this.projectMapper = projectMapper;
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        validateDateRange(request.startDate(), request.expectedEndDate());
        validateInitialAllocation(request.memberIds());

        Member manager = memberService.findEntityById(request.managerId());

        Project project = new Project(
                normalizeText(request.name()),
                request.startDate(),
                request.expectedEndDate(),
                request.totalBudget(),
                manager,
                ProjectStatus.EM_ANALISE
        );
        project.setDescription(normalizeNullableText(request.description()));

        Set<Member> allocatedMembers = resolveAndValidateMembersForAllocation(request.memberIds());
        allocatedMembers.forEach(project::addAllocatedMember);

        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return projectMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> findAll(ProjectFilterRequest filter, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");

        Specification<Project> specification = buildSpecification(filter);

        return projectRepository.findAll(specification, pageable)
                .map(projectMapper::toResponse);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        validateDateRange(request.startDate(), request.expectedEndDate());

        Project project = findEntityById(id);
        Member manager = memberService.findEntityById(request.managerId());

        project.setName(normalizeText(request.name()));
        project.setStartDate(request.startDate());
        project.setExpectedEndDate(request.expectedEndDate());
        project.setActualEndDate(request.actualEndDate());
        project.setTotalBudget(request.totalBudget());
        project.setDescription(normalizeNullableText(request.description()));
        project.setManager(manager);

        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional
    public void delete(Long id) {
        Project project = findEntityById(id);

        ProjectDeletionPolicy.validateCanDelete(project.getStatus());

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse changeStatus(Long id, ProjectStatusUpdateRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Project project = findEntityById(id);

        ProjectStatusTransitionPolicy.validateTransition(project.getStatus(), request.status());

        project.setStatus(request.status());

        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, Long memberId) {
        Project project = findEntityById(projectId);
        Member member = memberService.findEntityById(memberId);

        if (project.getAllocatedMembers().contains(member)) {
            return projectMapper.toResponse(project);
        }

        validateMemberCanBeAllocated(member);

        if (project.getAllocatedMembers().size() >= MAX_ALLOCATED_MEMBERS) {
            throw new BusinessException("O projeto não pode ter mais de 10 membros alocados.");
        }

        validateMemberActiveProjectLimit(member.getId());

        project.addAllocatedMember(member);

        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long memberId) {
        Project project = findEntityById(projectId);
        Member member = memberService.findEntityById(memberId);

        if (!project.getAllocatedMembers().contains(member)) {
            throw new BusinessException("Membro não está alocado neste projeto.");
        }

        if (project.getAllocatedMembers().size() <= MIN_ALLOCATED_MEMBERS) {
            throw new BusinessException("O projeto deve manter pelo menos 1 membro alocado.");
        }

        project.removeAllocatedMember(member);

        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public Project findEntityById(Long id) {
        Objects.requireNonNull(id, "id must not be null");

        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado."));
    }

    private Set<Member> resolveAndValidateMembersForAllocation(Set<Long> memberIds) {
        Set<Long> distinctMemberIds = new LinkedHashSet<>(memberIds);
        Set<Member> members = new LinkedHashSet<>();

        for (Long memberId : distinctMemberIds) {
            Member member = memberService.findEntityById(memberId);
            validateMemberCanBeAllocated(member);
            validateMemberActiveProjectLimit(member.getId());
            members.add(member);
        }

        return members;
    }

    private void validateInitialAllocation(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new BusinessException("O projeto deve ter pelo menos 1 membro alocado.");
        }

        int distinctMembers = new LinkedHashSet<>(memberIds).size();

        if (distinctMembers > MAX_ALLOCATED_MEMBERS) {
            throw new BusinessException("O projeto não pode ter mais de 10 membros alocados.");
        }
    }

    private void validateMemberCanBeAllocated(Member member) {
        if (member.getAssignment() != MemberAssignment.FUNCIONARIO) {
            throw new BusinessException("Apenas membros com atribuição funcionário podem ser alocados.");
        }
    }

    private void validateMemberActiveProjectLimit(Long memberId) {
        long activeProjects = projectRepository.countActiveProjectsByMemberId(memberId);

        if (activeProjects >= MAX_ACTIVE_PROJECTS_PER_MEMBER) {
            throw new BusinessException("O membro já está alocado em 3 projetos ativos.");
        }
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate expectedEndDate) {
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(expectedEndDate, "expectedEndDate must not be null");

        if (expectedEndDate.isBefore(startDate)) {
            throw new BusinessException("A previsão de término não pode ser anterior à data de início.");
        }
    }

    private Specification<Project> buildSpecification(ProjectFilterRequest filter) {
        if (filter == null) {
            return emptySpecification();
        }

        return ProjectSpecification.hasName(filter.name())
                .and(ProjectSpecification.hasStatus(filter.status()))
                .and(ProjectSpecification.hasManagerId(filter.managerId()))
                .and(ProjectSpecification.startDateBetween(filter.startDateFrom(), filter.startDateTo()))
                .and(ProjectSpecification.budgetBetween(filter.minBudget(), filter.maxBudget()));
    }

    private Specification<Project> emptySpecification() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Nome do projeto é obrigatório.");
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().replaceAll("\\s+", " ");
    }
}
