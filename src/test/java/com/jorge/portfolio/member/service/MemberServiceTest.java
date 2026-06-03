package com.jorge.portfolio.member.service;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.common.exception.ResourceNotFoundException;
import com.jorge.portfolio.member.dto.MemberCreateRequest;
import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.enums.MemberAssignment;
import com.jorge.portfolio.member.mapper.MemberMapper;
import com.jorge.portfolio.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberService memberService;

    @Test
    void shouldCreateMemberSuccessfully() {
        MemberCreateRequest request = new MemberCreateRequest(
                "  Ana   Souza  ",
                MemberAssignment.FUNCIONARIO
        );

        Member savedMember = new Member("Ana Souza", MemberAssignment.FUNCIONARIO);
        savedMember.setId(1L);

        MemberResponse expectedResponse = new MemberResponse(
                1L,
                "Ana Souza",
                MemberAssignment.FUNCIONARIO,
                null,
                null
        );

        when(memberRepository.existsByNameIgnoreCase("Ana Souza")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(memberMapper.toResponse(savedMember)).thenReturn(expectedResponse);

        MemberResponse response = memberService.create(request);

        assertThat(response).isEqualTo(expectedResponse);

        verify(memberRepository).existsByNameIgnoreCase("Ana Souza");
        verify(memberRepository).save(any(Member.class));
        verify(memberMapper).toResponse(savedMember);
    }

    @Test
    void shouldRejectDuplicatedMemberNameIgnoringCase() {
        MemberCreateRequest request = new MemberCreateRequest(
                "Ana Souza",
                MemberAssignment.FUNCIONARIO
        );

        when(memberRepository.existsByNameIgnoreCase("Ana Souza")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um membro cadastrado com este nome.");

        verify(memberRepository).existsByNameIgnoreCase("Ana Souza");
        verify(memberRepository, never()).save(any(Member.class));
        verifyNoInteractions(memberMapper);
    }

    @Test
    void shouldRejectBlankMemberName() {
        MemberCreateRequest request = new MemberCreateRequest(
                "   ",
                MemberAssignment.FUNCIONARIO
        );

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O nome do membro é obrigatório.");

        verifyNoInteractions(memberRepository);
        verifyNoInteractions(memberMapper);
    }

    @Test
    void shouldFindMemberById() {
        Member member = new Member("Ana Souza", MemberAssignment.FUNCIONARIO);
        member.setId(1L);

        MemberResponse expectedResponse = new MemberResponse(
                1L,
                "Ana Souza",
                MemberAssignment.FUNCIONARIO,
                null,
                null
        );

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMapper.toResponse(member)).thenReturn(expectedResponse);

        MemberResponse response = memberService.findById(1L);

        assertThat(response).isEqualTo(expectedResponse);

        verify(memberRepository).findById(1L);
        verify(memberMapper).toResponse(member);
    }

    @Test
    void shouldReturnEntityById() {
        Member member = new Member("Ana Souza", MemberAssignment.FUNCIONARIO);
        member.setId(1L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        Member result = memberService.findEntityById(1L);

        assertThat(result).isSameAs(member);
        verify(memberRepository).findById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundWhenMemberDoesNotExist() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Membro não encontrado.");

        verify(memberRepository).findById(99L);
        verifyNoInteractions(memberMapper);
    }
}