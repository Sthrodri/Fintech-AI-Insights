package com.projeto.domain.service;

import com.projeto.domain.entity.TenantStatus;
import com.projeto.domain.entity.Transaction;
import com.projeto.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public class ActiveTenantBalanceStrategy implements BalanceCalculationStrategy {

    @Override
    public boolean supports(TenantStatus status) {
        return status == TenantStatus.ACTIVE;
    }

    @Override
    public BigDecimal calculate(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.type() == TransactionType.ENTRADA
                        ? transaction.amount()
                        : transaction.amount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
