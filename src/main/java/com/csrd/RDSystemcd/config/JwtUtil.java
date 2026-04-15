package com.csrd.RDSystemcd.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key =
            Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkey".getBytes());

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    // ✅ GENERATE TOKEN
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // ROLE_ADMIN / ROLE_SUPER_ADMIN
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // ✅ ALL CLAIMS
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ USERNAME
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ ROLE
    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    // ✅ CHECK EXPIRED
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // ✅ VALIDATE TOKEN
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}