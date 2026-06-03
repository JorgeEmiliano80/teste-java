package com.jorge.portfolio.project.mapper;

import com.jorge.portfolio.member.dto.MemberResponse;
import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.member.mapper.MemberMapper;
import com.jorge.portfolio.project.domain.RiskClassificationCalculator;
import com.jorge.portfolio.project.dto.ProjectResponse;
import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.RiskClassification;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    private final MemberMapper memberMapper;

    public ProjectMapper(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        RiskClassification riskClassification = RiskClassificationCalculator.classify(
                project.getTotalBudget(),
                project.getStartDate(),
                project.getExpectedEndDate()
        );

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getActualEndDate(),
                project.getTotalBudget(),
                project.getDescription(),
                memberMapper.toResponse(project.getManager()),
                project.getStatus(),
                riskClassification,
                mapAllocatedMembers(project),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private Set<MemberResponse> mapAllocatedMembers(Project project) {
        if (project.getAllocatedMembers() == null || project.getAllocatedMembers().isEmpty()) {
            return Set.of();
        }

        return project.getAllocatedMembers()
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        Member::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(memberMapper::toResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}