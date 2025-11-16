package com.narciarz.benew.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 * 
 * <p>Provides beans for password encoding and will be extended later
 * with JWT authentication and role-based access control configuration.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Constructor injection of JWT authentication filter.
     * 
     * @param jwtAuthenticationFilter filter for JWT token validation
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
     * Configures Spring Security filter chain for non-test profiles.
     * 
     * <p>Security configuration:</p>
     * <ul>
     *   <li>CSRF disabled (stateless JWT authentication)</li>
     *   <li>Session management: STATELESS (no HTTP sessions)</li>
     *   <li>Public endpoints: /auth/login, Swagger UI, API docs</li>
     *   <li>All other endpoints require JWT authentication</li>
     *   <li>JWT filter added before UsernamePasswordAuthenticationFilter</li>
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
                        .requestMatchers(
                                "/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
