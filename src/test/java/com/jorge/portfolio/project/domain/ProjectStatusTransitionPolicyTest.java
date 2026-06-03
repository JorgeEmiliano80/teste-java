package com.jorge.portfolio.project.domain;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.project.enums.ProjectStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectStatusTransitionPolicyTest {

    @Test
    void shouldAllowNextSequentialStatusTransition() {
        assertThatCode(() -> ProjectStatusTransitionPolicy.validateTransition(
                ProjectStatus.EM_ANALISE,
                ProjectStatus.ANALISE_REALIZADA
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowTransitionToCanceledFromAnyStatus() {
        assertThatCode(() -> ProjectStatusTransitionPolicy.validateTransition(
                ProjectStatus.EM_ANDAMENTO,
                ProjectStatus.CANCELADO
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowSameStatusTransitionAsNoOp() {
        assertThatCode(() -> ProjectStatusTransitionPolicy.validateTransition(
                ProjectStatus.PLANEJADO,
                ProjectStatus.PLANEJADO
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSkippedStatusTransition() {
        assertThatThrownBy(() -> ProjectStatusTransitionPolicy.validateTransition(
                ProjectStatus.EM_ANALISE,
                ProjectStatus.ANALISE_APROVADA
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A transição de status deve respeitar a sequência lógica.");
    }

    @Test
    void shouldRejectTransitionFromCanceledToAnotherStatus() {
        assertThatThrownBy(() -> ProjectStatusTransitionPolicy.validateTransition(
                ProjectStatus.CANCELADO,
                ProjectStatus.EM_ANALISE
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Projetos cancelados não podem mudar de status.");
    }

    @Test
    void shouldReturnFalseWhenTransitionIsInvalid() {
        boolean result = ProjectStatusTransitionPolicy.canTransition(
                ProjectStatus.EM_ANALISE,
                ProjectStatus.INICIADO
        );

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenTransitionIsValid() {
        boolean result = ProjectStatusTransitionPolicy.canTransition(
                ProjectStatus.ANALISE_APROVADA,
                ProjectStatus.INICIADO
        );

        assertThat(result).isTrue();
    }
}