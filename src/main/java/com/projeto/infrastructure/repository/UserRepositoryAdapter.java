package com.projeto.infrastructure.repository;

import com.projeto.application.port.out.UserRepository;
import com.projeto.domain.entity.User;
import com.projeto.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository springDataUserJpaRepository;

    public UserRepositoryAdapter(SpringDataUserJpaRepository springDataUserJpaRepository) {
        this.springDataUserJpaRepository = springDataUserJpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserJpaRepository.findByEmail(email).map(UserEntity::toDomain);
    }
}
