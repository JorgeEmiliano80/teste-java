package com.jorge.portfolio.project.controller;

import com.jorge.portfolio.project.dto.ProjectCreateRequest;
import com.jorge.portfolio.project.dto.ProjectFilterRequest;
import com.jorge.portfolio.project.dto.ProjectResponse;
import com.jorge.portfolio.project.dto.ProjectStatusUpdateRequest;
import com.jorge.portfolio.project.dto.ProjectUpdateRequest;
import com.jorge.portfolio.project.enums.ProjectStatus;
import com.jorge.portfolio.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

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
            @PageableDefault(size = 20, sort = "id") Pageable pageable
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
}