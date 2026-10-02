package com.devsenior.gestorproductos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsenior.gestorproductos.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameIgnoreCase(String name);
}