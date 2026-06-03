package com.jorge.portfolio.member.repository;

import com.jorge.portfolio.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
