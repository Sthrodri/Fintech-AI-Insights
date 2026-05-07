package com.projeto.application.service;

import com.projeto.application.usecase.BudgetOptimizationItem;
import com.projeto.application.usecase.BudgetOptimizationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BudgetOptimizationServiceTest {

    private final BudgetOptimizationService service = new BudgetOptimizationService();

    @Test
    @DisplayName("Deve selecionar todas as contas quando o orçamento comporta tudo")
    void shouldSelectAllItemsWhenBudgetCoversEverything() {
        List<BudgetOptimizationItem> items = List.of(
                new BudgetOptimizationItem("A1", "Conta de energia", new BigDecimal("100.00"), new BigDecimal("8")),
                new BudgetOptimizationItem("B2", "Assinatura SaaS", new BigDecimal("50.00"), new BigDecimal("5")),
                new BudgetOptimizationItem("C3", "Investimento em marketing", new BigDecimal("25.00"), new BigDecimal("3"))
        );

        BudgetOptimizationResult result = service.optimize(new BigDecimal("200.00"), items);

        assertThat(result.selectedItems()).containsExactlyElementsOf(items);
        assertThat(result.totalAmount()).isEqualByComparingTo("175.00");
        assertThat(result.totalPriorityScore()).isEqualByComparingTo("16.00");
    }

    @Test
    @DisplayName("Deve maximizar a utilidade financeira quando é preciso escolher")
    void shouldMaximizeUtilityWhenBudgetRequiresChoice() {
        List<BudgetOptimizationItem> items = List.of(
                new BudgetOptimizationItem("A1", "Conta A", new BigDecimal("4.00"), new BigDecimal("5")),
                new BudgetOptimizationItem("B2", "Conta B", new BigDecimal("5.00"), new BigDecimal("6")),
                new BudgetOptimizationItem("C3", "Conta C", new BigDecimal("3.00"), new BigDecimal("4"))
        );

        BudgetOptimizationResult result = service.optimize(new BigDecimal("7.00"), items);

        assertThat(result.selectedItems()).extracting(BudgetOptimizationItem::referenceId).containsExactly("A1", "C3");
        assertThat(result.totalAmount()).isEqualByComparingTo("7.00");
        assertThat(result.totalPriorityScore()).isEqualByComparingTo("9.00");
    }

    @Test
    @DisplayName("Deve rejeitar orçamento zerado")
    void shouldRejectZeroBudget() {
        List<BudgetOptimizationItem> items = List.of(
                new BudgetOptimizationItem("A1", "Conta A", new BigDecimal("4.00"), new BigDecimal("5"))
        );

        assertThrows(IllegalArgumentException.class, () -> service.optimize(BigDecimal.ZERO, items));
    }
}
