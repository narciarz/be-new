package com.narciarz.benew.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * 
 * <p>Provides beans for password encoding and will be extended later
 * with JWT authentication and role-based access control configuration.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
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
     * <p>Allows public access to authentication endpoints and API documentation,
     * while requiring authentication for all other endpoints.</p>
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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
