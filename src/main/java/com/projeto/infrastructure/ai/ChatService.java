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
        if (openaiApiKey == null || openaiApiKey.isEmpty() || openaiApiKey.contains("sk-test")) {
            return "⚠️ Aviso: A chave da API OpenAI não está configurada corretamente. " +
                   "Configure a variável de ambiente OPENAI_API_KEY com uma chave válida. " +
                   "Alternativamente, use Ollama descomentando a configuração em application.yml";
        }
        String tenantId = tenantContextHolder.getTenantId();
        String safeContext = context == null || context.isBlank() ? "Sem transações recentes encontradas." : context;
        String systemPrompt = """
                Você é um consultor financeiro estritamente analítico e direto.
                Responda em no máximo 3 tópicos curtos.
                Não use saudações como "Olá" ou "Espero que ajude".
                Vá direto ao insight baseado nos dados fornecidos.
                Formate a resposta como tópicos curtos.
                Analise apenas os dados do tenant ativo.
                Tenant ativo: %s
                """.formatted(tenantId == null ? "desconhecido" : tenantId);
        String userPrompt = """
                Histórico recente do tenant:
                %s

                Gere um insight financeiro em linguagem natural, com 3 a 5 observações objetivas.
                """.formatted(safeContext);

        log.info("Prompt enviado ao Ollama:\nSYSTEM:\n{}\n\nUSER:\n{}", systemPrompt, userPrompt);

        return chatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
                .call()
                .content();
    }
}