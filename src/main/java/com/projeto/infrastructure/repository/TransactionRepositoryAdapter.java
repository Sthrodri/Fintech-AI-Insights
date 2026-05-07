package com.projeto.infrastructure.repository;

import com.projeto.application.port.out.TransactionRepository;
import com.projeto.domain.entity.Transaction;
import com.projeto.domain.valueobject.TenantId;
import com.projeto.domain.valueobject.TransactionId;
import com.projeto.infrastructure.persistence.entity.TransactionEntity;
import com.projeto.infrastructure.security.TenantContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final SpringDataTransactionJpaRepository springDataTransactionJpaRepository;
    private final TenantContextHolder tenantContextHolder;

    public TransactionRepositoryAdapter(
            SpringDataTransactionJpaRepository springDataTransactionJpaRepository,
            TenantContextHolder tenantContextHolder
    ) {
        this.springDataTransactionJpaRepository = springDataTransactionJpaRepository;
        this.tenantContextHolder = tenantContextHolder;
    }

    @Override
    public Transaction save(Transaction transaction) {
        validateTenantAccess(transaction.tenantId());
        TransactionEntity entity = TransactionEntity.fromDomain(transaction);
        TransactionEntity savedEntity = springDataTransactionJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        UUID tenantId = currentTenantId();
        return springDataTransactionJpaRepository.findByIdAndTenantId(id.value(), tenantId).map(TransactionEntity::toDomain);
    }

    @Override
    public List<Transaction> findAll() {
        UUID tenantId = currentTenantId();
        return springDataTransactionJpaRepository.findByTenantId(tenantId).stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findRecent(int limit) {
        int safeLimit = Math.max(1, limit);

        UUID tenantId = tryCurrentTenantId();
        if (tenantId == null) {
            return springDataTransactionJpaRepository
                    .findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                    .stream()
                    .map(TransactionEntity::toDomain)
                    .toList();
        }

        return springDataTransactionJpaRepository.findByTenantIdOrderByCreatedAtDesc(
                        tenantId,
                        PageRequest.of(0, safeLimit))
                .stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }

    private UUID tryCurrentTenantId() {
        String currentTenantId = tenantContextHolder.getTenantId();
        if (currentTenantId == null || currentTenantId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(currentTenantId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void validateTenantAccess(TenantId tenantId) {
        UUID currentTenantId = currentTenantId();
        if (!currentTenantId.toString().equals(tenantId.value().toString())) {
            throw new AccessDeniedException("tenant mismatch");
        }
    }

    private UUID currentTenantId() {
        String currentTenantId = tenantContextHolder.getTenantId();
        if (currentTenantId == null || currentTenantId.isBlank()) {
            throw new AccessDeniedException("tenant context is not available");
        }
        return UUID.fromString(currentTenantId);
    }
}
