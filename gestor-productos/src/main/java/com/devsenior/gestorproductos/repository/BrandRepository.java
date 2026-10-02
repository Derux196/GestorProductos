package com.devsenior.gestorproductos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsenior.gestorproductos.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByNameIgnoreCase(String name);
}