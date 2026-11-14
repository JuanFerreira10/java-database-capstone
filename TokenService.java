package com.smartclinic.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {

    // Llave secreta para firmar y verificar los tokens JWT (debe ser de al menos 256 bits)
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Tiempo de expiración del token en milisegundos (por defecto 24 horas)
    private final long expirationTime = 86400000;

    /**
     * Genera un token JWT para un usuario y rol específico.
     */
    public String generateToken(String identifier, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(identifier)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Valida si un token es correcto y pertenece al rol o usuario esperado.
     */
    public boolean validateToken(String token, String expectedRoleOrIdentifier) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);
            String subject = claims.getSubject();

            // Si el parámetro esperado coincide con el rol o con el subject (identificador/email)
            if (expectedRoleOrIdentifier.equalsIgnoreCase(role) ||
                expectedRoleOrIdentifier.equalsIgnoreCase(subject)) {
                return true;
            }

            // Validaciones especiales para roles genéricos permitidos en rutas
            if ("doctor".equalsIgnoreCase(expectedRoleOrIdentifier) && "DOCTOR".equalsIgnoreCase(role)) {
                return true;
            }
            if ("patient".equalsIgnoreCase(expectedRoleOrIdentifier) && "PATIENT".equalsIgnoreCase(role)) {
                return true;
            }
            if ("admin".equalsIgnoreCase(expectedRoleOrIdentifier) && "ADMIN".equalsIgnoreCase(role)) {
                return true;
            }

            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extrae el identificador (subject / email) contenido en el token.
     */
    public String extractIdentifier(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extrae el rol del usuario contenido en el token.
     */
    public String extractRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("role", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
