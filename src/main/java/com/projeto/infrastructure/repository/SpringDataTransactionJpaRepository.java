package com.projeto.infrastructure.repository;

import com.projeto.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

	Optional<TransactionEntity> findByIdAndTenantId(UUID id, UUID tenantId);

	List<TransactionEntity> findByTenantId(UUID tenantId);

	List<TransactionEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);
}
