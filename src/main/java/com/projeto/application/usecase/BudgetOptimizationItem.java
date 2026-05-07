package com.projeto.application.usecase;

import java.math.BigDecimal;
import java.util.Objects;

public record BudgetOptimizationItem(
        String referenceId,
        String description,
        BigDecimal amount,
        BigDecimal priorityScore
) {

    public BudgetOptimizationItem {
        validateText(referenceId, "referenceId is required");
        validateText(description, "description is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(priorityScore, "priorityScore is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (priorityScore.signum() < 0) {
            throw new IllegalArgumentException("priorityScore must be greater than or equal to zero");
        }
    }

    private static void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
