package com.jorge.portfolio.project.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class ProjectDurationCalculator {

    private ProjectDurationCalculator() {
    }

    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static boolean isWithinMonths(LocalDate startDate, LocalDate endDate, long months) {
        validateDateRange(startDate, endDate);
        return !endDate.isAfter(startDate.plusMonths(months));
    }

    public static boolean isLongerThanMonths(LocalDate startDate, LocalDate endDate, long months) {
        validateDateRange(startDate, endDate);
        return endDate.isAfter(startDate.plusMonths(months));
    }

    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "starteDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }
}