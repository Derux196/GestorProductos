package com.devsenior.gestorproductos.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devsenior.gestorproductos.dto.ProductRequest;
import com.devsenior.gestorproductos.dto.ProductResponse;
import com.devsenior.gestorproductos.entity.Brand;
import com.devsenior.gestorproductos.entity.Category;
import com.devsenior.gestorproductos.entity.Product;
import com.devsenior.gestorproductos.repository.BrandRepository;
import com.devsenior.gestorproductos.repository.CategoryRepository;
import com.devsenior.gestorproductos.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya existe");
        }
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
        Product product = new Product(
                request.name(), request.description(), request.price(), category, brand);
        return toResponse(productRepository.save(product));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getName(),
                product.getBrand().getName());
    }
}