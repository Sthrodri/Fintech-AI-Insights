package com.projeto.api.mapper;

import com.projeto.api.dto.BudgetOptimizationItemRequest;
import com.projeto.api.dto.BudgetOptimizationItemResponse;
import com.projeto.api.dto.BudgetOptimizationRequest;
import com.projeto.api.dto.BudgetOptimizationResponse;
import com.projeto.api.dto.FinancialInsightResponse;
import com.projeto.application.usecase.BudgetOptimizationItem;
import com.projeto.application.usecase.BudgetOptimizationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinancialAnalysisMapper {

    public List<BudgetOptimizationItem> toUseCaseItems(BudgetOptimizationRequest request) {
        return request.items().stream()
                .map(this::toUseCaseItem)
                .toList();
    }

    public BudgetOptimizationItem toUseCaseItem(BudgetOptimizationItemRequest request) {
        return new BudgetOptimizationItem(
                request.referenceId(),
                request.description(),
                request.amount(),
                request.priorityScore()
        );
    }

    public BudgetOptimizationResponse toResponse(BudgetOptimizationResult result) {
        return new BudgetOptimizationResponse(
                result.selectedItems().stream()
                        .map(this::toResponseItem)
                        .toList(),
                result.totalAmount(),
                result.totalPriorityScore()
        );
    }

    public BudgetOptimizationItemResponse toResponseItem(BudgetOptimizationItem item) {
        return new BudgetOptimizationItemResponse(
                item.referenceId(),
                item.description(),
                item.amount(),
                item.priorityScore()
        );
    }

    public FinancialInsightResponse toInsightResponse(String insight) {
        return new FinancialInsightResponse(insight);
    }
}