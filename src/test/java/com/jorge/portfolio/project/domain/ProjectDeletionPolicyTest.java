package com.jorge.portfolio.project.domain;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.project.enums.ProjectStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectDeletionPolicyTest {

    @Test
    void shouldAllowDeletingProjectInAnalysisStatus() {
        assertThatCode(() -> ProjectDeletionPolicy.validateCanDelete(ProjectStatus.EM_ANALISE))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowDeletingCanceledProject() {
        assertThatCode(() -> ProjectDeletionPolicy.validateCanDelete(ProjectStatus.CANCELADO))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDeletingStartedProject() {
        assertThatThrownBy(() -> ProjectDeletionPolicy.validateCanDelete(ProjectStatus.INICIADO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Projetos com status iniciado, em andamento ou encerrado não podem ser excluídos.");
    }

    @Test
    void shouldRejectDeletingProjectInProgress() {
        assertThatThrownBy(() -> ProjectDeletionPolicy.validateCanDelete(ProjectStatus.EM_ANDAMENTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Projetos com status iniciado, em andamento ou encerrado não podem ser excluídos.");
    }

    @Test
    void shouldRejectDeletingClosedProject() {
        assertThatThrownBy(() -> ProjectDeletionPolicy.validateCanDelete(ProjectStatus.ENCERRADO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Projetos com status iniciado, em andamento ou encerrado não podem ser excluídos.");
    }

    @Test
    void shouldReturnFalseWhenProjectCannotBeDeleted() {
        boolean result = ProjectDeletionPolicy.canDelete(ProjectStatus.ENCERRADO);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenProjectCanBeDeleted() {
        boolean result = ProjectDeletionPolicy.canDelete(ProjectStatus.ANALISE_REALIZADA);

        assertThat(result).isTrue();
    }
}