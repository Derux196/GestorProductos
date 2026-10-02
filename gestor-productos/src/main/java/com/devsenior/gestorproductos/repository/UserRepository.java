package com.devsenior.gestorproductos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsenior.gestorproductos.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}