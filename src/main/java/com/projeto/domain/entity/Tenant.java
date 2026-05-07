package com.projeto.domain.entity;

import java.time.Instant;
import java.util.Objects;

import com.projeto.domain.valueobject.TenantId;

public record Tenant(
        TenantId id,
        String legalName,
        String tradeName,
        String document,
        TenantStatus status,
        Instant createdAt
) {

    public Tenant {
        Objects.requireNonNull(id, "tenant id is required");
        validateText(legalName, "legal name is required");
        validateText(tradeName, "trade name is required");
        validateText(document, "document is required");
        Objects.requireNonNull(status, "tenant status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    private static void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
