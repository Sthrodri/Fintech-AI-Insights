package com.projeto.application.port.out;

import com.projeto.domain.entity.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);
}