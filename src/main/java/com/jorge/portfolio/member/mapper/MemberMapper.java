package com.jorge.portfolio.member.mapper;

import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberResponse toResponse(Member member) {
        if (member == null) {
            return null;
        }

        return new MemberResponse(
            member.getId(),
            member.getName(),
            member.getAssignment(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}