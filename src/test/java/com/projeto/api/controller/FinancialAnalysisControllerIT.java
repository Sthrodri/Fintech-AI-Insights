package com.projeto.api.controller;

import com.projeto.api.mapper.FinancialAnalysisMapper;
import com.projeto.application.usecase.BudgetOptimizationItem;
import com.projeto.application.usecase.BudgetOptimizationResult;
import com.projeto.application.usecase.BudgetOptimizationUseCase;
import com.projeto.application.usecase.GenerateFinancialInsightsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinancialAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FinancialAnalysisMapper.class)
class FinancialAnalysisControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetOptimizationUseCase budgetOptimizationUseCase;

    @MockBean
    private GenerateFinancialInsightsUseCase generateFinancialInsightsUseCase;

    @Test
    @DisplayName("Deve retornar a otimização do orçamento com JSON estruturado")
    void shouldReturnOptimizedBudgetJson() throws Exception {
        when(budgetOptimizationUseCase.optimize(any(), any())).thenReturn(
                new BudgetOptimizationResult(
                        List.of(
                                new BudgetOptimizationItem("A1", "Conta de energia", new BigDecimal("100.00"), new BigDecimal("8")),
                                new BudgetOptimizationItem("C3", "Marketing", new BigDecimal("25.00"), new BigDecimal("3"))
                        ),
                        new BigDecimal("125.00"),
                        new BigDecimal("11.00")
                )
        );

        String requestJson = """
                {
                  "availableBudget": 150.00,
                  "items": [
                    {"referenceId": "A1", "description": "Conta de energia", "amount": 100.00, "priorityScore": 8},
                    {"referenceId": "B2", "description": "Assinatura SaaS", "amount": 80.00, "priorityScore": 7},
                    {"referenceId": "C3", "description": "Marketing", "amount": 25.00, "priorityScore": 3}
                  ]
                }
                """;

        mockMvc.perform(post("/analysis/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedItems[0].referenceId").value("A1"))
                .andExpect(jsonPath("$.selectedItems[1].referenceId").value("C3"))
                .andExpect(jsonPath("$.totalAmount").value(125.00))
                .andExpect(jsonPath("$.totalPriorityScore").value(11.00));
    }

    @Test
    @DisplayName("Deve retornar insights financeiros em formato JSON")
    void shouldReturnInsightsJson() throws Exception {
        when(generateFinancialInsightsUseCase.analyzeTenantHistory()).thenReturn("Insight do tenant ativo");

        mockMvc.perform(get("/analysis/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insight").value("Insight do tenant ativo"));
    }
}
