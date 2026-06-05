package com.jorge.portfolio.project.specification;

import com.jorge.portfolio.member.entity.Member;
import com.jorge.portfolio.project.entity.Project;
import com.jorge.portfolio.project.enums.ProjectStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectSpecificationTest {

    @Mock
    private Root<Project> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate predicate;

    @Test
    void hasNameShouldReturnConjunctionWhenNameIsNull() {
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        Predicate result = ProjectSpecification.hasName(null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).conjunction();
        verifyNoInteractions(root);
    }

    @Test
    void hasNameShouldReturnConjunctionWhenNameIsBlank() {
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        Predicate result = ProjectSpecification.hasName("   ")
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).conjunction();
        verifyNoInteractions(root);
    }

    @Test
    void hasNameShouldCreateLikePredicateWhenNameHasValue() {
        Path<String> namePath = path();
        Expression<String> loweredName = expression();

        when(root.<String>get("name")).thenReturn(namePath);
        when(criteriaBuilder.lower(namePath)).thenReturn(loweredName);
        when(criteriaBuilder.like(loweredName, "%data%")).thenReturn(predicate);

        Predicate result = ProjectSpecification.hasName("Data")
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("name");
        verify(criteriaBuilder).lower(namePath);
        verify(criteriaBuilder).like(loweredName, "%data%");
    }

    @Test
    void hasStatusShouldReturnConjunctionWhenStatusIsNull() {
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        Predicate result = ProjectSpecification.hasStatus(null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).conjunction();
        verifyNoInteractions(root);
    }

    @Test
    void hasStatusShouldCreateEqualPredicateWhenStatusHasValue() {
        Path<ProjectStatus> statusPath = path();

        when(root.<ProjectStatus>get("status")).thenReturn(statusPath);
        when(criteriaBuilder.equal(statusPath, ProjectStatus.EM_ANALISE)).thenReturn(predicate);

        Predicate result = ProjectSpecification.hasStatus(ProjectStatus.EM_ANALISE)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("status");
        verify(criteriaBuilder).equal(statusPath, ProjectStatus.EM_ANALISE);
    }

    @Test
    void hasManagerIdShouldReturnConjunctionWhenManagerIdIsNull() {
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        Predicate result = ProjectSpecification.hasManagerId(null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).conjunction();
        verifyNoInteractions(root);
    }

    @Test
    void hasManagerIdShouldCreateEqualPredicateWhenManagerIdHasValue() {
        Path<Member> managerPath = path();
        Path<Long> managerIdPath = path();

        when(root.<Member>get("manager")).thenReturn(managerPath);
        when(managerPath.<Long>get("id")).thenReturn(managerIdPath);
        when(criteriaBuilder.equal(managerIdPath, 1L)).thenReturn(predicate);

        Predicate result = ProjectSpecification.hasManagerId(1L)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("manager");
        verify(managerPath).get("id");
        verify(criteriaBuilder).equal(managerIdPath, 1L);
    }

    @Test
    void startDateBetweenShouldReturnConjunctionWhenDatesAreNull() {
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        Predicate result = ProjectSpecification.startDateBetween(null, null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).conjunction();
        verifyNoInteractions(root);
    }

    @Test
    void startDateBetweenShouldCreateGreaterThanOrEqualPredicateWhenOnlyFromIsPresent() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        Path<LocalDate> startDatePath = path();

        when(root.<LocalDate>get("startDate")).thenReturn(startDatePath);
        when(criteriaBuilder.greaterThanOrEqualTo(startDatePath, from)).thenReturn(predicate);

        Predicate result = ProjectSpecification.startDateBetween(from, null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("startDate");
        verify(criteriaBuilder).greaterThanOrEqualTo(startDatePath, from);
    }

    @Test
    void startDateBetweenShouldCreateLessThanOrEqualPredicateWhenOnlyToIsPresent() {
        LocalDate to = LocalDate.of(2026, 12, 31);
        Path<LocalDate> startDatePath = path();

        when(root.<LocalDate>get("startDate")).thenReturn(startDatePath);
        when(criteriaBuilder.lessThanOrEqualTo(startDatePath, to)).thenReturn(predicate);

        Predicate result = ProjectSpecification.startDateBetween(null, to)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("startDate");
        verify(criteriaBuilder).lessThanOrEqualTo(startDatePath, to);
    }

    @Test
    void startDateBetweenShouldCreateBetweenPredicateWhenFromAndToArePresent() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        Path<LocalDate> startDatePath = path();

        when(root.<LocalDate>get("startDate")).thenReturn(startDatePath);
        when(criteriaBuilder.between(startDatePath, from, to)).thenReturn(predicate);

        Predicate result = ProjectSpecification.startDateBetween(from, to)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("startDate");
        verify(criteriaBuilder).between(startDatePath, from, to);
    }

    @Test
    void budgetBetweenShouldReturnConjunctionWhenBudgetsAreNull() {
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        Predicate result = ProjectSpecification.budgetBetween(null, null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).conjunction();
        verifyNoInteractions(root);
    }

    @Test
    void budgetBetweenShouldCreateGreaterThanOrEqualPredicateWhenOnlyMinIsPresent() {
        BigDecimal min = new BigDecimal("1000.00");
        Path<BigDecimal> totalBudgetPath = path();

        when(root.<BigDecimal>get("totalBudget")).thenReturn(totalBudgetPath);
        when(criteriaBuilder.greaterThanOrEqualTo(totalBudgetPath, min)).thenReturn(predicate);

        Predicate result = ProjectSpecification.budgetBetween(min, null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("totalBudget");
        verify(criteriaBuilder).greaterThanOrEqualTo(totalBudgetPath, min);
    }

    @Test
    void budgetBetweenShouldCreateLessThanOrEqualPredicateWhenOnlyMaxIsPresent() {
        BigDecimal max = new BigDecimal("500000.00");
        Path<BigDecimal> totalBudgetPath = path();

        when(root.<BigDecimal>get("totalBudget")).thenReturn(totalBudgetPath);
        when(criteriaBuilder.lessThanOrEqualTo(totalBudgetPath, max)).thenReturn(predicate);

        Predicate result = ProjectSpecification.budgetBetween(null, max)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("totalBudget");
        verify(criteriaBuilder).lessThanOrEqualTo(totalBudgetPath, max);
    }

    @Test
    void budgetBetweenShouldCreateBetweenPredicateWhenMinAndMaxArePresent() {
        BigDecimal min = new BigDecimal("1000.00");
        BigDecimal max = new BigDecimal("500000.00");
        Path<BigDecimal> totalBudgetPath = path();

        when(root.<BigDecimal>get("totalBudget")).thenReturn(totalBudgetPath);
        when(criteriaBuilder.between(totalBudgetPath, min, max)).thenReturn(predicate);

        Predicate result = ProjectSpecification.budgetBetween(min, max)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).get("totalBudget");
        verify(criteriaBuilder).between(totalBudgetPath, min, max);
    }

    @SuppressWarnings("unchecked")
    private <T> Path<T> path() {
        return mock(Path.class);
    }

    @SuppressWarnings("unchecked")
    private <T> Expression<T> expression() {
        return mock(Expression.class);
    }
}
