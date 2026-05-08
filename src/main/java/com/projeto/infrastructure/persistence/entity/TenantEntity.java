package com.projeto.infrastructure.persistence.entity;

import com.projeto.domain.entity.Tenant;
import com.projeto.domain.entity.TenantStatus;
import com.projeto.domain.valueobject.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantEntity {

    @Id
    private UUID id;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trade_name", nullable = false)
    private String tradeName;

    @Column(nullable = false)
    private String document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TenantEntity() {
    }

    private TenantEntity(
            UUID id,
            String legalName,
            String tradeName,
            String document,
            TenantStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.document = document;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static TenantEntity fromDomain(Tenant tenant) {
        return new TenantEntity(
                tenant.id().value(),
                tenant.legalName(),
                tenant.tradeName(),
                tenant.document(),
                tenant.status(),
                tenant.createdAt()
        );
    }

    public Tenant toDomain() {
        return new Tenant(
                new TenantId(id),
                legalName,
                tradeName,
                document,
                status,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
