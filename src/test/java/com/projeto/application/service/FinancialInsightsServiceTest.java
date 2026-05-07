package com.projeto.application.service;

import com.projeto.application.port.out.FinancialInsightPort;
import com.projeto.application.port.out.TransactionRepository;
import com.projeto.domain.entity.Transaction;
import com.projeto.domain.entity.TransactionType;
import com.projeto.domain.valueobject.TenantId;
import com.projeto.domain.valueobject.TransactionId;
import com.projeto.infrastructure.security.TenantContextHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class FinancialInsightsServiceTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final FinancialInsightPort financialInsightPort = mock(FinancialInsightPort.class);
        private final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        private final FinancialInsightsService service = new FinancialInsightsService(
                        transactionRepository,
                        financialInsightPort,
                        tenantContextHolder
        );

    @Test
    @DisplayName("Deve orquestrar o RAG consultando o repositório e repassando o contexto para a IA")
    void shouldOrchestrateRagFlow() {
        Transaction first = new Transaction(
                new TransactionId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                new TenantId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
                "Recebimento cliente X",
                new BigDecimal("1500.00"),
                TransactionType.ENTRADA,
                "Receita",
                Instant.parse("2026-05-01T10:15:30Z")
        );
        Transaction second = new Transaction(
                new TransactionId(UUID.fromString("33333333-3333-3333-3333-333333333333")),
                new TenantId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
                "Pagamento fornecedor Y",
                new BigDecimal("800.00"),
                TransactionType.SAIDA,
                "Operacional",
                Instant.parse("2026-05-02T11:20:30Z")
        );

        when(transactionRepository.findRecent(20)).thenReturn(List.of(first, second));
        when(financialInsightPort.generateInsight(org.mockito.ArgumentMatchers.anyString())).thenReturn("Insight gerado");

        String insight = service.analyzeTenantHistory();

        assertThat(insight).isEqualTo("Insight gerado");
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(financialInsightPort).generateInsight(contextCaptor.capture());
        verify(transactionRepository).findRecent(20);
        assertThat(contextCaptor.getValue()).isEqualTo("""
                2026-05-01T10:15:30Z | Recebimento cliente X | Receita | ENTRADA | 1500.00
                2026-05-02T11:20:30Z | Pagamento fornecedor Y | Operacional | SAIDA | 800.00
                """.stripTrailing());
    }
}
