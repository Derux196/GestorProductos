package com.devsenior.gestorproductos.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devsenior.gestorproductos.entity.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(AppUser user) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("rol", user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}