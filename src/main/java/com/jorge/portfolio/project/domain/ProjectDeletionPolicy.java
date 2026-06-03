package com.jorge.portfolio.project.domain;

import com.jorge.portfolio.common.exception.BusinessException;
import com.jorge.portfolio.project.enums.ProjectStatus;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class ProjectDeletionPolicy {
    
    private static final Set<ProjectStatus> BLOCKED_DELETION_STATUSES = EnumSet.of(
        ProjectStatus.INICIADO,
        ProjectStatus.EM_ANDAMENTO,
        ProjectStatus.ENCERRADO
    );

    private ProjectDeletionPolicy() {
    }

    public static void validateCanDelete(ProjectStatus status) {
        Objects.requireNonNull(status, "status must not be null");

        if (BLOCKED_DELETION_STATUSES.contains(status)) {
            throw new BusinessException("Projetos com status iniciado, em andamento ou encerrado não podem ser excluídos.");
        }
    }

    public static boolean canDelete(ProjectStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return !BLOCKED_DELETION_STATUSES.contains(status);
    }
}
