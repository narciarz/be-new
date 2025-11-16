package com.narciarz.benew.services;

import com.narciarz.benew.exceptions.InvalidCredentialsException;
import com.narciarz.benew.models.AppUser;
import com.narciarz.benew.models.dto.LoginRequestDto;
import com.narciarz.benew.models.dto.LoginResponseDto;
import com.narciarz.benew.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user authentication operations.
 * 
 * <p>Handles user login, credential verification, and JWT token generation.
 * Uses BCrypt for password verification and delegates token generation to {@link JwtService}.</p>
 * 
 * <p>Security considerations:</p>
 * <ul>
 *   <li>Passwords are never logged or exposed in error messages</li>
 *   <li>Generic error messages prevent user enumeration attacks</li>
 *   <li>Failed login attempts are logged for security monitoring</li>
 *   <li>Uses constant-time password comparison via BCrypt</li>
 * </ul>
 */
@Service
public class AuthenticationService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    /**
     * Constructor injection of dependencies.
     * 
     * @param userRepository repository for user data access
     * @param passwordEncoder BCrypt password encoder
     * @param jwtService service for JWT token operations
     */
    public AuthenticationService(
            UserRepository userRepository, 
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    /**
     * Authenticates user credentials and generates JWT token.
     * 
     * <p>Authentication flow:</p>
     * <ol>
     *   <li>Look up user by email (case-insensitive)</li>
     *   <li>Verify password against stored BCrypt hash</li>
     *   <li>Generate JWT token with user claims</li>
     *   <li>Return login response with token and user details</li>
     * </ol>
     * 
     * <p>For security, the error message is intentionally generic
     * ("Invalid email or password") regardless of whether the email exists
     * or the password is wrong. This prevents attackers from enumerating
     * valid user accounts.</p>
     * 
     * @param request login request containing email and password
     * @return login response with JWT token and user information
     * @throws InvalidCredentialsException if email or password is invalid
     */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        log.debug("Attempting login for email: {}", request.getEmail());
        
        // Look up user by email (case-insensitive)
        AppUser user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email: {}", request.getEmail());
                    return new InvalidCredentialsException();
                });
        
        // Verify password using BCrypt (constant-time comparison)
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException();
        }
        
        // Generate JWT token
        String token = jwtService.generateToken(user);
        
        log.info("User successfully authenticated: {} (role: {})", user.getEmail(), user.getRole());
        
        // Build and return login response
        return new LoginResponseDto(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}

