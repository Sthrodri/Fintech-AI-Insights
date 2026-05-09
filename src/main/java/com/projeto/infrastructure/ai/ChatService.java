package com.projeto.infrastructure.ai;

import com.projeto.application.port.out.FinancialInsightPort;
import com.projeto.infrastructure.security.TenantContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatService implements FinancialInsightPort {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final TenantContextHolder tenantContextHolder;

    @Value("${spring.ai.openai.api-key:}")
    private String openaiApiKey;

    public ChatService(ChatClient.Builder chatClientBuilder, TenantContextHolder tenantContextHolder) {
        this.chatClient = chatClientBuilder.build();
        this.tenantContextHolder = tenantContextHolder;
    }

    @Override
    public String generateInsight(String context) {
        String tenantId = tenantContextHolder.getTenantId();
        String safeContext = context == null || context.isBlank() ? "Sem transações recentes encontradas." : context;
        String systemPrompt = """
                Você é um analista financeiro backend de uma plataforma SaaS.
                
                Regras:
                - Responda em português brasileiro
                - Seja técnico e objetivo
                - Não invente dados
                - Não repita as transações
                - Não explique o que recebeu
                - Gere apenas insights financeiros
                - Use no máximo 5 tópicos
                - Cada tópico deve ter no máximo 2 linhas
                - Analise somente os dados do tenant atual
                
                Tenant ativo: %s
            """.formatted(tenantId == null ? "desconhecido" : tenantId);
        String userPrompt = """
                Analise as transações abaixo e gere insights financeiros.
                
                Considere:
                - padrões de gastos
                - categorias mais caras
                - equilíbrio entre entradas e saídas
                - possíveis excessos
                - observações relevantes
                
                Transações:
                %s
            """.formatted(safeContext);

        log.info("Prompt enviado ao Ollama:\nSYSTEM:\n{}\n\nUSER:\n{}", systemPrompt, userPrompt);

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}