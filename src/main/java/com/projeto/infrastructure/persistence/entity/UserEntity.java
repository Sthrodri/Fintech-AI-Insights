package com.projeto.infrastructure.persistence.entity;

import com.projeto.domain.entity.User;
import com.projeto.domain.valueobject.TenantId;
import com.projeto.domain.valueobject.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected UserEntity() {
    }

    private UserEntity(
            UUID id,
            UUID tenantId,
            String email,
            String passwordHash,
            Instant createdAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public static UserEntity fromDomain(User user) {
        return new UserEntity(
                user.id().value(),
                user.tenantId().value(),
                user.email(),
                user.passwordHash(),
                user.createdAt()
        );
    }

    public User toDomain() {
        return new User(
                new UserId(id),
                new TenantId(tenantId),
                email,
                passwordHash,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
