package com.jorge.portfolio.project.domain;

import com.jorge.portfolio.project.enums.RiskClassification;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskClassificationCalculatorTest {

    @Test
    void shouldClassifyAsLowRiskWhenBudgetIsUpToOneHundredThousandAndDurationIsUpToThreeMonths() {
        RiskClassification result = RiskClassificationCalculator.classify(
                new BigDecimal("100000.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1)
        );

        assertThat(result).isEqualTo(RiskClassification.BAIXO);
    }

    @Test
    void shouldClassifyAsMediumRiskWhenBudgetIsBetweenOneHundredThousandAndFiveHundredThousand() {
        RiskClassification result = RiskClassificationCalculator.classify(
                new BigDecimal("250000.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 1)
        );

        assertThat(result).isEqualTo(RiskClassification.MEDIO);
    }

    @Test
    void shouldClassifyAsMediumRiskWhenDurationIsGreaterThanThreeMonthsAndUpToSixMonths() {
        RiskClassification result = RiskClassificationCalculator.classify(
                new BigDecimal("90000.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1)
        );

        assertThat(result).isEqualTo(RiskClassification.MEDIO);
    }

    @Test
    void shouldClassifyAsHighRiskWhenBudgetIsGreaterThanFiveHundredThousand() {
        RiskClassification result = RiskClassificationCalculator.classify(
                new BigDecimal("500000.01"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
        );

        assertThat(result).isEqualTo(RiskClassification.ALTO);
    }

    @Test
    void shouldClassifyAsHighRiskWhenDurationIsGreaterThanSixMonths() {
        RiskClassification result = RiskClassificationCalculator.classify(
                new BigDecimal("100000.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 2)
        );

        assertThat(result).isEqualTo(RiskClassification.ALTO);
    }

    @Test
    void shouldThrowExceptionWhenExpectedEndDateIsBeforeStartDate() {
        assertThatThrownBy(() -> RiskClassificationCalculator.classify(
                new BigDecimal("100000.00"),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endDate must not be before startDate");
    }
}