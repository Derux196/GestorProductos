package com.devsenior.gestorproductos.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devsenior.gestorproductos.dto.BrandRequest;
import com.devsenior.gestorproductos.dto.BrandResponse;
import com.devsenior.gestorproductos.entity.Brand;
import com.devsenior.gestorproductos.repository.BrandRepository;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAll() {
        return brandRepository.findAll().stream()
                .map(brand -> new BrandResponse(brand.getId(), brand.getName()))
                .toList();
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La marca ya existe");
        }
        Brand saved = brandRepository.save(new Brand(request.name()));
        return new BrandResponse(saved.getId(), saved.getName());
    }
}