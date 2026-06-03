package com.jorge.portfolio.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjectCreateRequest(
        @NotBlank
        @Size(max = 160)
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate expectedEndDate,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal totalBudget,

        @Size(max = 2000)
        String description,

        @NotNull
        Long managerId,

        Set<Long> memberIds
) {
}
