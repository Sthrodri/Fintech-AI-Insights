package com.projeto.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "user id is required");
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }
}
