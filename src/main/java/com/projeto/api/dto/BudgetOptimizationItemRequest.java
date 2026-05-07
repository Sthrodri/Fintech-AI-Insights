package com.projeto.api.dto;

import java.math.BigDecimal;

public record BudgetOptimizationItemRequest(
        String referenceId,
        String description,
        BigDecimal amount,
        BigDecimal priorityScore
) {
}