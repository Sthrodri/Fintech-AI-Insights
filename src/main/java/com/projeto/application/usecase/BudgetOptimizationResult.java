package com.projeto.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record BudgetOptimizationResult(
        List<BudgetOptimizationItem> selectedItems,
        BigDecimal totalAmount,
        BigDecimal totalPriorityScore
) {

    public BudgetOptimizationResult {
        Objects.requireNonNull(selectedItems, "selectedItems is required");
        Objects.requireNonNull(totalAmount, "totalAmount is required");
        Objects.requireNonNull(totalPriorityScore, "totalPriorityScore is required");
    }
}
