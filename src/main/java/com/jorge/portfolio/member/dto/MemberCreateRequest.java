package com.jorge.portfolio.member.dto;

import com.jorge.portfolio.member.enums.MemberAssignment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotNull
        MemberAssignment assignment
) {
}
