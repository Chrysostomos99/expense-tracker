package com.example.expense_tracker.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    public String generateToken(String email) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(signingKey)
                .compact();
    }

    public Optional<String> extractEmailIfValid(String token) {

        try {
            String email = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return Optional.of(email);

        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}