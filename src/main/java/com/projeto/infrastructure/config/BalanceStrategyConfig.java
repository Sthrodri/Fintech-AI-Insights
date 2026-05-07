package com.projeto.infrastructure.config;

import com.projeto.domain.service.ActiveTenantBalanceStrategy;
import com.projeto.domain.service.DegradedTenantBalanceStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BalanceStrategyConfig {

    @Bean
    public ActiveTenantBalanceStrategy activeTenantBalanceStrategy() {
        return new ActiveTenantBalanceStrategy();
    }

    @Bean
    public DegradedTenantBalanceStrategy degradedTenantBalanceStrategy() {
        return new DegradedTenantBalanceStrategy();
    }
}
