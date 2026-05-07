package com.projeto.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetOptimizationResponse(
        List<BudgetOptimizationItemResponse> selectedItems,
        BigDecimal totalAmount,
        BigDecimal totalPriorityScore
) {
}