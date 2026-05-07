package com.projeto.application.service;

import com.projeto.application.port.out.FinancialInsightPort;
import com.projeto.application.port.out.TransactionRepository;
import com.projeto.application.usecase.GenerateFinancialInsightsUseCase;
import com.projeto.domain.entity.Transaction;
import com.projeto.infrastructure.security.TenantContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialInsightsService implements GenerateFinancialInsightsUseCase {

    private static final int RECENT_TRANSACTION_LIMIT = 20;
    private static final String DEMO_TENANT_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
    private static final String INSUFFICIENT_DATA_MESSAGE = "Dados insuficientes para gerar insights financeiros.";

    private static final Logger log = LoggerFactory.getLogger(FinancialInsightsService.class);

    private final TransactionRepository transactionRepository;
    private final FinancialInsightPort financialInsightPort;
    private final TenantContextHolder tenantContextHolder;

    public FinancialInsightsService(
            TransactionRepository transactionRepository,
            FinancialInsightPort financialInsightPort,
            TenantContextHolder tenantContextHolder
    ) {
        this.transactionRepository = transactionRepository;
        this.financialInsightPort = financialInsightPort;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Override
    public String analyzeTenantHistory() {
        if (tenantContextHolder.getTenantId() == null || tenantContextHolder.getTenantId().isBlank()) {
            tenantContextHolder.setTenantId(DEMO_TENANT_ID);
            log.info("Tenant context ausente. Aplicando fallback temporário de demo: {}", DEMO_TENANT_ID);
        }

        List<Transaction> recentTransactions = transactionRepository.findRecent(RECENT_TRANSACTION_LIMIT);
        log.info("Insights request loaded {} transaction(s) from database.", recentTransactions.size());

        if (recentTransactions.isEmpty()) {
            return INSUFFICIENT_DATA_MESSAGE;
        }

        String context = recentTransactions.stream()
                .map(transaction -> String.join(" | ",
                        transaction.createdAt().toString(),
                        transaction.description(),
                        transaction.category(),
                        transaction.type().name(),
                        transaction.amount().toPlainString()))
                .collect(Collectors.joining("\n"));
        return financialInsightPort.generateInsight(context);
    }
}
