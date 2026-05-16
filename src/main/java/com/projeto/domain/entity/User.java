package com.projeto.domain.entity;

import com.projeto.domain.valueobject.TenantId;
import com.projeto.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public record User(
        UserId id,
        TenantId tenantId,
        String email,
        String passwordHash,
        Instant createdAt
) {

    public User {
        Objects.requireNonNull(id, "user id is required");
        Objects.requireNonNull(tenantId, "tenant id is required");
        validateEmail(email);
        validateText(passwordHash, "password hash is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("invalid email format");
        }
    }

    private static void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
