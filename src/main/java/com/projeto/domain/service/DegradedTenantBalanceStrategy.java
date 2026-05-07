package com.projeto.domain.service;

import com.projeto.domain.entity.TenantStatus;
import com.projeto.domain.entity.Transaction;
import com.projeto.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DegradedTenantBalanceStrategy implements BalanceCalculationStrategy {

    private static final BigDecimal CONSERVATIVE_FACTOR = new BigDecimal("0.90");

    @Override
    public boolean supports(TenantStatus status) {
        return status == TenantStatus.INACTIVE || status == TenantStatus.SUSPENDED;
    }

    @Override
    public BigDecimal calculate(List<Transaction> transactions) {
        BigDecimal rawBalance = transactions.stream()
            .map(transaction -> transaction.type() == TransactionType.ENTRADA
                ? transaction.amount()
                : transaction.amount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
            .multiply(CONSERVATIVE_FACTOR);
        return rawBalance.setScale(2, RoundingMode.HALF_UP);
    }
}
