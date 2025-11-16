package com.narciarz.benew.controllers;

import com.narciarz.benew.models.dto.LoginRequestDto;
import com.narciarz.benew.models.dto.LoginResponseDto;
import com.narciarz.benew.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * 
 * <p>Handles user authentication operations including login.
 * This controller is publicly accessible (no JWT required) to allow
 * users to obtain their initial authentication token.</p>
 * 
 * <p>Endpoints:</p>
 * <ul>
 *   <li>POST /auth/login - Authenticate user and receive JWT token</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and token management endpoints")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationService authenticationService;
    
    /**
     * Constructor injection of authentication service.
     * 
     * @param authenticationService service handling authentication logic
     */
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    /**
     * Authenticates user credentials and returns JWT token.
     * 
     * <p>POST /auth/login</p>
     * 
     * <p>The request body must contain valid email and password.
     * Bean Validation ensures required fields are present and properly formatted.</p>
     * 
     * <p>On successful authentication, returns:</p>
     * <ul>
     *   <li>JWT token for subsequent API calls (include in Authorization header as Bearer token)</li>
     *   <li>User ID, email, role, first name, and last name</li>
     * </ul>
     * 
     * <p>Error scenarios:</p>
     * <ul>
     *   <li>400 Bad Request - Invalid request format or validation errors</li>
     *   <li>401 Unauthorized - Invalid email or password</li>
     *   <li>500 Internal Server Error - Unexpected server error</li>
     * </ul>
     * 
     * @param request login request containing email and password
     * @return HTTP 200 OK with login response containing JWT token and user details
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user credentials and obtain JWT token for API access"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request format or validation errors"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid email or password"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.debug("Received login request for email: {}", request.getEmail());
        
        LoginResponseDto response = authenticationService.login(request);
        
        return ResponseEntity.ok(response);
    }
}

