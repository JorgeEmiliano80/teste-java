package com.jorge.portfolio.project.domain;

import com.jorge.portfolio.project.enums.RiskClassification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class RiskClassificationCalculator {
    
    private static final BigDecimal LOW_RISK_MAX_BUDGET = new BigDecimal("100000.00");
    private static final BigDecimal HIGH_RISK_MIN_BUDGET_EXCLUSIVE = new BigDecimal("500000.00");

    private static final long LOW_RISK_MAX_MONTHS = 3;
    private static final long HIGH_RISK_MAX_MONTHS = 6;

    private RiskClassificationCalculator(){
    }

    public static RiskClassification classify(
        BigDecimal totalBudget,
        LocalDate startDate,
        LocalDate expectedEndDate
    ) {
        Objects.requireNonNull(totalBudget,"totalBudget must not be null");
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(expectedEndDate, "expectedEndDate must not be null");

        if (isHighRisk(totalBudget, startDate, expectedEndDate)) {
            return RiskClassification.ALTO;
        }

        if (isLowRisk(totalBudget, startDate, expectedEndDate)) {
            return RiskClassification.BAIXO;
        }

        return RiskClassification.MEDIO;
    }

    private static boolean isLowRisk(
        BigDecimal totalBudget,
        LocalDate startDate,
        LocalDate expectedEndDate
    ) {
        return totalBudget.compareTo(LOW_RISK_MAX_BUDGET) <= 0
                && ProjectDurationCalculator.isWithinMonths(startDate, expectedEndDate, LOW_RISK_MAX_MONTHS);
    }

    private static boolean isHighRisk(
        BigDecimal totalBudget,
        LocalDate startDate,
        LocalDate expectedEndDate
    ) {
        return totalBudget.compareTo(HIGH_RISK_MIN_BUDGET_EXCLUSIVE) > 0
                || ProjectDurationCalculator.isLongerThanMonths(startDate, expectedEndDate, HIGH_RISK_MAX_MONTHS);
    }
}
