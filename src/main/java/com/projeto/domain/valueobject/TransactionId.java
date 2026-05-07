package com.projeto.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID value) {

    public TransactionId {
        Objects.requireNonNull(value, "transaction id is required");
    }

    public static TransactionId random() {
        return new TransactionId(UUID.randomUUID());
    }
}
