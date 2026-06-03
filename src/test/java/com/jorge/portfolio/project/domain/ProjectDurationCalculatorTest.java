package com.jorge.portfolio.project.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectDurationCalculatorTest {

    @Test
    void shouldCalculateDaysBetweenDates() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        long result = ProjectDurationCalculator.daysBetween(startDate, endDate);

        assertThat(result).isEqualTo(30);
    }

    @Test
    void shouldReturnTrueWhenEndDateIsWithinGivenMonths() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 1);

        boolean result = ProjectDurationCalculator.isWithinMonths(startDate, endDate, 3);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEndDateIsAfterGivenMonthsLimit() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 2);

        boolean result = ProjectDurationCalculator.isWithinMonths(startDate, endDate, 3);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenProjectIsLongerThanGivenMonths() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 2);

        boolean result = ProjectDurationCalculator.isLongerThanMonths(startDate, endDate, 6);

        assertThat(result).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsBeforeStartDate() {
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> ProjectDurationCalculator.daysBetween(startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endDate must not be before startDate");
    }
}