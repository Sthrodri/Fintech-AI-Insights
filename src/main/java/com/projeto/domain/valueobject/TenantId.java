package com.projeto.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TenantId(UUID value) {

    public TenantId {
        Objects.requireNonNull(value, "tenant id is required");
    }

    public static TenantId random() {
        return new TenantId(UUID.randomUUID());
    }
}
