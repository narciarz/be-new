package com.narciarz.benew.config;

import com.narciarz.benew.services.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;

/**
 * Security configuration for the application.
 * 
 * <p>Configures Spring Security with OAuth2 Resource Server using JWT authentication.
 * Provides role-based access control for different endpoints.</p>
 * 
 * <p>Architecture:</p>
 * <ul>
 *   <li>Uses Spring Security OAuth2 Resource Server with JWT</li>
 *   <li>Stateless session management (no HTTP sessions)</li>
 *   <li>Role-based access control per endpoint</li>
 *   <li>Method-level security with @PreAuthorize annotations</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable @PreAuthorize, @PostAuthorize etc.
public class SecurityConfig {
    
    private final JwtService jwtService;
    
    /**
     * Constructor injection of JWT service.
     * 
     * @param jwtService service for JWT operations (provides signing key)
     */
    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    /**
     * Provides a BCrypt password encoder bean.
     * 
     * <p>BCrypt is a strong hashing algorithm with built-in salting,
     * appropriate for secure password storage.</p>
     * 
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures Spring Security filter chain.
     * 
     * <p>Security configuration:</p>
     * <ul>
     *   <li>CSRF disabled (appropriate for stateless JWT authentication)</li>
     *   <li>Publicly accessible endpoints: /auth/login, /swagger-ui/**, /v3/api-docs/**</li>
     *   <li>All other endpoints require authentication (to be enforced with JWT filter)</li>
     * </ul>
     * 
     * <p>Note: In test profile, all requests are permitted for easier testing.</p>
     * 
     * @param http HttpSecurity configuration
     * @return configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    @Profile({"test"}) // Activate this permissive configuration in test profile
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
    
    /**
     * Provides JWT decoder for OAuth2 Resource Server.
     * 
     * <p>Uses the same secret key as JwtService for token validation.</p>
     * 
     * @return JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = jwtService.getSigningKey();
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
    
    /**
     * Configures JWT authentication converter.
     * 
     * <p>Maps JWT claims to Spring Security authorities.
     * Adds "ROLE_" prefix to the role claim for compatibility with
     * Spring Security's role-based access control.</p>
     * 
     * @return JWT authentication converter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Map "role" claim to authorities with "ROLE_" prefix
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        
        return jwtAuthenticationConverter;
    }
    
    /**
     * Configures Spring Security filter chain for non-test profiles.
     * 
     * <p>Security configuration with role-based access control:</p>
     * <ul>
     *   <li>CSRF disabled (stateless JWT authentication)</li>
     *   <li>Session management: STATELESS (no HTTP sessions)</li>
     *   <li>Public endpoints: /auth/login, Swagger UI, API docs</li>
     *   <li>OAuth2 Resource Server with JWT decoder</li>
     *   <li>Role-based authorization per endpoint pattern</li>
     * </ul>
     * 
     * <p>Role-based access control rules:</p>
     * <ul>
     *   <li><b>ADMIN</b>: Full access to all endpoints (users, templates, onboarding)</li>
     *   <li><b>MANAGER</b>: Read users, manage team's onboarding processes</li>
     *   <li><b>USER</b>: Read own user data, view own onboarding process</li>
     * </ul>
     * 
     * @param http HttpSecurity configuration
     * @return configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    @Profile({"!test"}) // Activate for all profiles except test
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        
                        // User management endpoints
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "MANAGER")
                        
                        // Template management endpoints (admin only)
                        .requestMatchers("/api/templates/**").hasRole("ADMIN")
                        
                        // Onboarding process endpoints
                        .requestMatchers("/api/onboarding/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        
        return http.build();
    }
}
