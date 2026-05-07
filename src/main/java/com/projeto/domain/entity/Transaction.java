package com.projeto.domain.entity;

import com.projeto.domain.valueobject.TenantId;
import com.projeto.domain.valueobject.TransactionId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Transaction(
        TransactionId id,
        TenantId tenantId,
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        Instant createdAt
) {

    public Transaction {
        Objects.requireNonNull(id, "transaction id is required");
        Objects.requireNonNull(tenantId, "tenant id is required");
        validateText(description, "description is required");
        Objects.requireNonNull(amount, "amount is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        Objects.requireNonNull(type, "transaction type is required");
        validateText(category, "category is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    private static void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
