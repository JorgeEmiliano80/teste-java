package com.jorge.portfolio.member.mapper;

import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.enums.MemberAssignment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberMapperTest {

    private final MemberMapper memberMapper = new MemberMapper();

    @Test
    void shouldMapMemberToResponse() {
        Member member = new Member("Ana Souza", MemberAssignment.FUNCIONARIO);
        member.setId(1L);

        MemberResponse response = memberMapper.toResponse(member);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Ana Souza");
        assertThat(response.assignment()).isEqualTo(MemberAssignment.FUNCIONARIO);
    }

    @Test
    void shouldReturnNullWhenMemberIsNull() {
        MemberResponse response = memberMapper.toResponse(null);

        assertThat(response).isNull();
    }
}