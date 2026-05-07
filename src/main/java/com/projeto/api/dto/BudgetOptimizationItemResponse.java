package com.projeto.api.dto;

import java.math.BigDecimal;

public record BudgetOptimizationItemResponse(
        String referenceId,
        String description,
        BigDecimal amount,
        BigDecimal priorityScore
) {
}