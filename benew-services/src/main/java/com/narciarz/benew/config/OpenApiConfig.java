package com.narciarz.benew.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI/Swagger documentation.
 * 
 * <p>Configures Swagger UI with JWT Bearer token authentication.
 * This allows testing authenticated endpoints directly from Swagger UI.</p>
 * 
 * <p>To use JWT authentication in Swagger UI:</p>
 * <ol>
 *   <li>First call POST /auth/login to get JWT token</li>
 *   <li>Click "Authorize" button in Swagger UI</li>
 *   <li>Enter the token (without "Bearer " prefix)</li>
 *   <li>Click "Authorize" to save the token</li>
 *   <li>All subsequent requests will include the Authorization header</li>
 * </ol>
 */
@Configuration
public class OpenApiConfig {
    
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    
    /**
     * Configures OpenAPI specification with JWT Bearer authentication.
     * 
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Be New API")
                        .version("1.0.0")
                        .description("REST API for Be New - Employee Onboarding Management System")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from POST /auth/login")));
    }
}

