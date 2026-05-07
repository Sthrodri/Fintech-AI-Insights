package com.projeto.application.usecase;

import com.projeto.domain.entity.Tenant;
import com.projeto.domain.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface CalculateCurrentBalanceUseCase {

    BigDecimal calculate(Tenant tenant, List<Transaction> transactions);
}
