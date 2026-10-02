package com.devsenior.gestorproductos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsenior.gestorproductos.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}