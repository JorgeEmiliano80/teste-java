package com.jorge.portfolio.project.domain;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.project.enums.ProjectStatus;

import java.util.List;
import java.util.Objects;

public final class ProjectStatusTransitionPolicy {
    
    private static final List<ProjectStatus> SEQUENTIAL_STATUSES = List.of(
        ProjectStatus.EM_ANALISE,
        ProjectStatus.ANALISE_REALIZADA,
        ProjectStatus.ANALISE_APROVADA,
        ProjectStatus.INICIADO,
        ProjectStatus.PLANEJADO,
        ProjectStatus.EM_ANDAMENTO,
        ProjectStatus.ENCERRADO
    );

    private ProjectStatusTransitionPolicy() {
    }

    public static void validateTransition(ProjectStatus currentStatus, ProjectStatus targetStatus) {
        Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        Objects.requireNonNull(targetStatus, "targetStatus must not be null");

        if (currentStatus == targetStatus) {
            return;
        }

        if (targetStatus == ProjectStatus.CANCELADO){
            return;
        }

        if (currentStatus == ProjectStatus.CANCELADO) {
            throw new BusinessException("Projetos cancelados não podem mudar de status.");
        }

        int currentIndex = SEQUENTIAL_STATUSES.indexOf(currentStatus);
        int targetIndex = SEQUENTIAL_STATUSES.indexOf(targetStatus);

        if (currentIndex < 0 || targetIndex < 0) {
            throw new BusinessException("Status invalido para transição.");
        }

        if (targetIndex != currentIndex + 1) {
            throw new BusinessException("A transição de status deve respeitar a sequência lógica.");
        }
    }

    public static boolean canTransition(ProjectStatus currentStatus, ProjectStatus targetStatus) {
        try {
            validateTransition(currentStatus, targetStatus);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }
}
