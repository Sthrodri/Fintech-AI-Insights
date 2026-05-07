package com.projeto.api.controller;

import com.projeto.api.dto.BudgetOptimizationRequest;
import com.projeto.api.dto.BudgetOptimizationResponse;
import com.projeto.api.dto.FinancialInsightResponse;
import com.projeto.api.mapper.FinancialAnalysisMapper;
import com.projeto.application.usecase.BudgetOptimizationUseCase;
import com.projeto.application.usecase.GenerateFinancialInsightsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analysis")
@Tag(name = "Financial Analysis", description = "Endpoints de otimização e análise financeira com IA")
public class FinancialAnalysisController {

    private final BudgetOptimizationUseCase budgetOptimizationUseCase;
    private final GenerateFinancialInsightsUseCase generateFinancialInsightsUseCase;
    private final FinancialAnalysisMapper financialAnalysisMapper;

    public FinancialAnalysisController(
            BudgetOptimizationUseCase budgetOptimizationUseCase,
            GenerateFinancialInsightsUseCase generateFinancialInsightsUseCase,
            FinancialAnalysisMapper financialAnalysisMapper
    ) {
        this.budgetOptimizationUseCase = budgetOptimizationUseCase;
        this.generateFinancialInsightsUseCase = generateFinancialInsightsUseCase;
        this.financialAnalysisMapper = financialAnalysisMapper;
    }

    @PostMapping("/optimize")
    @Operation(summary = "Otimiza o orçamento usando programação dinâmica")
    @ApiResponse(responseCode = "200", description = "Retorna a melhor alocação possível do orçamento",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BudgetOptimizationResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida")
    public ResponseEntity<BudgetOptimizationResponse> optimize(@Valid @RequestBody BudgetOptimizationRequest request) {
        var result = budgetOptimizationUseCase.optimize(
                request.availableBudget(),
                financialAnalysisMapper.toUseCaseItems(request)
        );
        return ResponseEntity.ok(financialAnalysisMapper.toResponse(result));
    }

    @GetMapping("/insights")
    @Operation(summary = "Gera insights financeiros com RAG e Spring AI")
    @ApiResponse(responseCode = "200", description = "Retorna um insight financeiro em linguagem natural",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = FinancialInsightResponse.class)))
    public ResponseEntity<FinancialInsightResponse> insights() {
        String insight = generateFinancialInsightsUseCase.analyzeTenantHistory();
        return ResponseEntity.ok(financialAnalysisMapper.toInsightResponse(insight));
    }
}