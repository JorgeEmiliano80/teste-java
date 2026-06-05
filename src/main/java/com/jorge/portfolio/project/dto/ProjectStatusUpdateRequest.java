package com.jorge.portfolio.project.dto;

import com.jorge.portfolio.project.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusUpdateRequest(
        @NotNull(message = "Status é obrigatório.")
        ProjectStatus status
) {
}
