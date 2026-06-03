package com.jorge.portfolio.member.repository;

import com.jorge.portfolio.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
