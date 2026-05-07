package com.projeto.application.service;

import com.projeto.application.usecase.CalculateCurrentBalanceUseCase;
import com.projeto.domain.entity.Tenant;
import com.projeto.domain.entity.Transaction;
import com.projeto.domain.service.ActiveTenantBalanceStrategy;
import com.projeto.domain.service.DegradedTenantBalanceStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TenantBalanceService implements CalculateCurrentBalanceUseCase {

    private final ActiveTenantBalanceStrategy activeTenantBalanceStrategy;
    private final DegradedTenantBalanceStrategy degradedTenantBalanceStrategy;

    public TenantBalanceService(
            ActiveTenantBalanceStrategy activeTenantBalanceStrategy,
            DegradedTenantBalanceStrategy degradedTenantBalanceStrategy
    ) {
        this.activeTenantBalanceStrategy = activeTenantBalanceStrategy;
        this.degradedTenantBalanceStrategy = degradedTenantBalanceStrategy;
    }

    @Override
    public BigDecimal calculate(Tenant tenant, List<Transaction> transactions) {
        var strategy = switch (tenant.status()) {
            case ACTIVE -> activeTenantBalanceStrategy;
            case INACTIVE, SUSPENDED -> degradedTenantBalanceStrategy;
        };
        return strategy.calculate(transactions);
    }
}
