package com.jorge.portfolio.project.controller;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.project.dto.ProjectCreateRequest;
import com.jorge.portfolio.project.dto.ProjectFilterRequest;
import com.jorge.portfolio.project.dto.ProjectResponse;
import com.jorge.portfolio.project.dto.ProjectStatusUpdateRequest;
import com.jorge.portfolio.project.dto.ProjectUpdateRequest;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "name",
            "startDate",
            "expectedEndDate",
            "actualEndDate",
            "totalBudget",
            "status",
            "createdAt",
            "updatedAt"
    );

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

@GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDateTo,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection
    ) {
        ProjectFilterRequest filter = new ProjectFilterRequest(
                name,
                status,
                managerId,
                startDateFrom,
                startDateTo,
                minBudget,
                maxBudget
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(projectService.findAll(filter, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProjectStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(projectService.changeStatus(id, request));
    }

    @PostMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(projectService.addMember(projectId, memberId));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<ProjectResponse> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(projectService.removeMember(projectId, memberId));
    }

    private Pageable createPageable(
            int page,
            int size,
            String sortBy,
            Sort.Direction sortDirection
    ) {
        if (page < 0) {
            throw new BusinessException("O número da página não pode ser negativo.");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException("O tamanho da página deve estar entre 1 e 100.");
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException("Campo de ordenação inválido.");
        }

        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}