package com.narciarz.benew.config;

import com.narciarz.benew.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT Authentication Filter.
 * 
 * <p>Intercepts HTTP requests and validates JWT tokens from the Authorization header.
 * If a valid token is found, creates an authentication object and sets it in the
 * Spring Security context.</p>
 * 
 * <p>Filter workflow:</p>
 * <ol>
 *   <li>Extract JWT token from Authorization header (Bearer scheme)</li>
 *   <li>Validate token signature and expiration</li>
 *   <li>Extract user claims (userId, email, role)</li>
 *   <li>Create authentication object with authorities</li>
 *   <li>Set authentication in SecurityContext</li>
 * </ol>
 * 
 * <p>This filter is executed once per request before reaching the controller.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtService jwtService;
    
    /**
     * Constructor injection of JWT service.
     * 
     * @param jwtService service for JWT operations
     */
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    /**
     * Filters incoming requests to validate JWT tokens.
     * 
     * <p>If no Authorization header or invalid token, the filter chain continues
     * without setting authentication (request will be rejected by Spring Security
     * if endpoint requires authentication).</p>
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain filter chain to continue processing
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Extract Authorization header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No JWT token found in request to: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extract token (remove "Bearer " prefix and trim whitespace)
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            
            // Log token info for debugging (only first/last few chars for security)
            if (log.isDebugEnabled() && token.length() > 20) {
                log.debug("Processing JWT token: {}...{}", 
                        token.substring(0, 10), 
                        token.substring(token.length() - 10));
            }
            
            // Validate token
            if (!jwtService.validateToken(token)) {
                log.warn("Invalid or expired JWT token for request to: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
            
            // Extract claims from token
            UUID userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            
            // Check if user is not already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Create authorities from role
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );
                
                // Create authentication token
                // Principal: userId (as String)
                // Credentials: null (not needed after authentication)
                // Authorities: role-based authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        authorities
                );
                
                // Set additional details (request info)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("User authenticated: {} (role: {}) for request to: {}", 
                        email, role, request.getRequestURI());
            }
            
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}

