package com.projeto.application.port.out;

import com.projeto.domain.entity.Transaction;
import com.projeto.domain.valueobject.TransactionId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(TransactionId id);

    List<Transaction> findAll();

    List<Transaction> findRecent(int limit);
}