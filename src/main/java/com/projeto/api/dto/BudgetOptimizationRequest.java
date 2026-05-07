package com.projeto.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetOptimizationRequest(
        BigDecimal availableBudget,
        List<BudgetOptimizationItemRequest> items
) {
}