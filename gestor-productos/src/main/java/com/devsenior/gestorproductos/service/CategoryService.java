package com.devsenior.gestorproductos.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devsenior.gestorproductos.dto.CategoryRequest;
import com.devsenior.gestorproductos.dto.CategoryResponse;
import com.devsenior.gestorproductos.entity.Category;
import com.devsenior.gestorproductos.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName()))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría ya existe");
        }
        Category saved = categoryRepository.save(new Category(request.name()));
        return new CategoryResponse(saved.getId(), saved.getName());
    }
}