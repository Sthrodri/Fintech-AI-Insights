package com.projeto.application.usecase;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetOptimizationUseCase {

    BudgetOptimizationResult optimize(BigDecimal availableBudget, List<BudgetOptimizationItem> candidates);
}
