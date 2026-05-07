package com.projeto.infrastructure.persistence.entity;

import com.projeto.domain.entity.Transaction;
import com.projeto.domain.entity.TransactionType;
import com.projeto.domain.valueobject.TenantId;
import com.projeto.domain.valueobject.TransactionId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TransactionEntity() {
    }

    private TransactionEntity(
            UUID id,
            UUID tenantId,
            String description,
            BigDecimal amount,
            TransactionType type,
            String category,
            Instant createdAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.createdAt = createdAt;
    }

    public static TransactionEntity fromDomain(Transaction transaction) {
        return new TransactionEntity(
                transaction.id().value(),
                transaction.tenantId().value(),
                transaction.description(),
                transaction.amount(),
                transaction.type(),
                transaction.category(),
                transaction.createdAt()
        );
    }

    public Transaction toDomain() {
        return new Transaction(
                new TransactionId(id),
                new TenantId(tenantId),
                description,
                amount,
                type,
                category,
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
