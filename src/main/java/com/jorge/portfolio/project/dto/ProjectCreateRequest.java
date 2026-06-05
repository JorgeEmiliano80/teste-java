package com.jorge.portfolio.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjectCreateRequest(
        @NotBlank(message = "Nome do projeto é obrigatório.")
        @Size(max = 160, message = "Nome do projeto deve ter no máximo 160 caracteres.")
        String name,

        @NotNull(message = "Data de início é obrigatória.")
        LocalDate startDate,

        @NotNull(message = "Previsão de término é obrigatória.")
        LocalDate expectedEndDate,

        @NotNull(message = "Orçamento total é obrigatório.")
        @DecimalMin(value = "0.01", message = "Orçamento total deve ser maior ou igual a 0,01.")
        BigDecimal totalBudget,

        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres.")
        String description,

        @NotNull(message = "Gerente é obrigatório.")
        Long managerId,

        Set<Long> memberIds
) {
}
