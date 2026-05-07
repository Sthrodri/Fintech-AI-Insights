package com.projeto.application.service;

import com.projeto.application.usecase.BudgetOptimizationItem;
import com.projeto.application.usecase.BudgetOptimizationResult;
import com.projeto.application.usecase.BudgetOptimizationUseCase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetOptimizationService implements BudgetOptimizationUseCase {

    @Override
    public BudgetOptimizationResult optimize(BigDecimal availableBudget, List<BudgetOptimizationItem> candidates) {
        if (availableBudget == null || availableBudget.signum() <= 0) {
            throw new IllegalArgumentException("availableBudget must be greater than zero");
        }
        if (candidates == null || candidates.isEmpty()) {
            return new BudgetOptimizationResult(List.of(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        int budgetInCents = toCents(availableBudget);
        int itemCount = candidates.size();
        BigDecimal[][] dp = new BigDecimal[itemCount + 1][budgetInCents + 1];
        boolean[][] keep = new boolean[itemCount + 1][budgetInCents + 1];

        for (int i = 0; i <= itemCount; i++) {
            for (int w = 0; w <= budgetInCents; w++) {
                dp[i][w] = BigDecimal.ZERO;
            }
        }

        for (int i = 1; i <= itemCount; i++) {
            BudgetOptimizationItem item = candidates.get(i - 1);
            int itemWeight = toCents(item.amount());
            BigDecimal itemValue = item.priorityScore();

            for (int w = 0; w <= budgetInCents; w++) {
                BigDecimal withoutItem = dp[i - 1][w];
                BigDecimal withItem = BigDecimal.valueOf(-1);
                if (itemWeight <= w) {
                    withItem = dp[i - 1][w - itemWeight].add(itemValue);
                }
                if (withItem.compareTo(withoutItem) > 0) {
                    dp[i][w] = withItem;
                    keep[i][w] = true;
                } else {
                    dp[i][w] = withoutItem;
                }
            }
        }

        List<BudgetOptimizationItem> selectedItems = new ArrayList<>();
        int remainingBudget = budgetInCents;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalPriority = BigDecimal.ZERO;

        for (int i = itemCount; i >= 1; i--) {
            if (keep[i][remainingBudget]) {
                BudgetOptimizationItem item = candidates.get(i - 1);
                selectedItems.add(item);
                remainingBudget -= toCents(item.amount());
                totalAmount = totalAmount.add(item.amount());
                totalPriority = totalPriority.add(item.priorityScore());
            }
        }

        Collections.reverse(selectedItems);
        return new BudgetOptimizationResult(
                selectedItems,
                totalAmount.setScale(2, RoundingMode.HALF_UP),
                totalPriority.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private int toCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .intValueExact();
    }
}
