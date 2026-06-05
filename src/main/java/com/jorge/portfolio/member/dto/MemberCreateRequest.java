package com.jorge.portfolio.member.dto;

import com.jorge.portfolio.member.enums.MemberAssignment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank(message = "O nome do membro é obrigatório.")
        @Size(max = 120, message = "O nome do membro deve ter no máximo 120 caracteres.")
        String name,

        @NotNull(message = "A atribuição do membro é obrigatória.")
        MemberAssignment assignment
) {
}
