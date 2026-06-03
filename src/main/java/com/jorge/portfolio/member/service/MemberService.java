package com.jorge.portfolio.member.service;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.common.exception.ResourceNotFoundException;
import com.jorge.portfolio.member.dto.MemberCreateRequest;
import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.mapper.MemberMapper;
import com.jorge.portfolio.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberService(MemberRepository memberRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String normalizedName = normalizeName(request.name());

        if (memberRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("Já existe um membro cadastrado com este nome.");
        }

        Member member = new Member(normalizedName, request.assignment());
        Member savedMember = memberRepository.save(member);

        return memberMapper.toResponse(savedMember);
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return memberMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Member findEntityById(Long id) {
        Objects.requireNonNull(id, "id must not be null");

        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado."));
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("O nome do membro é obrigatório.");
        }

        return name.trim().replaceAll("\\s+", " ");
    }
}