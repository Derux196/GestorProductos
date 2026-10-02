package com.devsenior.gestorproductos.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devsenior.gestorproductos.dto.LoginRequest;
import com.devsenior.gestorproductos.dto.RegisterRequest;
import com.devsenior.gestorproductos.dto.TokenResponse;
import com.devsenior.gestorproductos.entity.AppUser;
import com.devsenior.gestorproductos.repository.UserRepository;
import com.devsenior.gestorproductos.security.Role;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        AppUser user = userRepository.save(new AppUser(
                request.email(), passwordEncoder.encode(request.password()), Role.USER));
        return new TokenResponse(jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .filter(existing -> passwordEncoder.matches(request.password(), existing.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        return new TokenResponse(jwtService.generateToken(user));
    }
}