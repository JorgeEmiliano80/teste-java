package com.jorge.portfolio.member.dto;

import com.jorge.portfolio.member.enums.MemberAssignment;
import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String name,
        MemberAssignment assignment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
