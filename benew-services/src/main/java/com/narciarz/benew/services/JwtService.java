package com.narciarz.benew.services;

import com.narciarz.benew.models.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for JWT token generation and validation.
 * 
 * <p>Handles creation, parsing, and validation of JWT tokens used for authentication.
 * Tokens include user ID, email, and role as claims.</p>
 * 
 * <p>Security considerations:</p>
 * <ul>
 *   <li>Uses HS256 algorithm for token signing</li>
 *   <li>Secret key should be at least 256 bits (32 bytes)</li>
 *   <li>Tokens have configurable expiration time</li>
 *   <li>Secret key stored in application configuration (externalized)</li>
 * </ul>
 */
@Service
public class JwtService {
    
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    
    /**
     * Secret key for JWT signing. Should be at least 256 bits.
     * Configured via application.yml property: jwt.secret
     */
    @Value("${jwt.secret}")
    private String secretKey;
    
    /**
     * JWT token expiration time in milliseconds.
     * Configured via application.yml property: jwt.expiration
     * Default: 24 hours (86400000 ms)
     */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    /**
     * Generates a JWT token for the authenticated user.
     * 
     * <p>Token includes following claims:</p>
     * <ul>
     *   <li>sub (subject): user ID</li>
     *   <li>email: user email address</li>
     *   <li>role: user role (ADMIN, MANAGER, USER)</li>
     * </ul>
     * 
     * @param user authenticated user
     * @return JWT token string
     */
    public String generateToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        String token = Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
        
        log.debug("Generated JWT token for user: {} (expires at: {})", user.getEmail(), expiryDate);
        return token;
    }
    
    /**
     * Extracts user ID from JWT token.
     * 
     * @param token JWT token
     * @return user ID
     */
    public UUID extractUserId(String token) {
        String subject = extractAllClaims(token).getSubject();
        return UUID.fromString(subject);
    }
    
    /**
     * Extracts user email from JWT token.
     * 
     * @param token JWT token
     * @return user email
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }
    
    /**
     * Extracts user role from JWT token.
     * 
     * @param token JWT token
     * @return user role as string
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    
    /**
     * Checks if JWT token is expired.
     * 
     * @param token JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
    
    /**
     * Validates JWT token.
     * 
     * <p>Token is valid if:</p>
     * <ul>
     *   <li>It can be parsed successfully</li>
     *   <li>It's not expired</li>
     *   <li>Signature is valid</li>
     * </ul>
     * 
     * @param token JWT token
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts all claims from JWT token.
     * 
     * @param token JWT token
     * @return claims object
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Gets the signing key for JWT operations.
     * 
     * <p>Exposed as public to allow SecurityConfig to configure
     * Spring Security's JWT decoder with the same secret key.</p>
     * 
     * @return secret key for signing
     */
    public SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

