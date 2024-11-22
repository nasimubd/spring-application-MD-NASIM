package com.epartner.userManagementService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;


    public String generateToken(String username, List<String> roles) {
        log.debug("Generating token for username: {} with roles: {}", username, roles);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token valid for 10 hours
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public boolean validateToken(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);

            // Debug the username validation
            log.debug("Validating token for username: {}", extractedUsername);

            boolean isTokenValid = extractedUsername.equals(username) && !isTokenExpired(token);
            log.debug("Token valid: {}", isTokenValid);

            return isTokenValid;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }


    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // Extract roles from token
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        List<String> roles = claims.get("roles", List.class);

        // Debug the roles extracted from the token
        log.debug("Roles extracted from token: {}", roles);

        return roles;
    }
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

}
