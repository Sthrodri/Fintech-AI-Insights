package com.projeto.domain.service;

import com.projeto.domain.entity.TenantStatus;
import com.projeto.domain.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface BalanceCalculationStrategy {

    boolean supports(TenantStatus status);

    BigDecimal calculate(List<Transaction> transactions);
}
