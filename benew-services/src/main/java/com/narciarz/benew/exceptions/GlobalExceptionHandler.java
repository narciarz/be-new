package com.narciarz.benew.exceptions;

import com.narciarz.benew.models.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API endpoints.
 * 
 * <p>Centralizes exception handling across all controllers using {@code @RestControllerAdvice}.
 * Maps exceptions to appropriate HTTP status codes and returns consistent error responses
 * using {@link ErrorResponseDto}.</p>
 * 
 * <p>Handles:</p>
 * <ul>
 *   <li>Business logic exceptions (user not found, duplicate email, etc.)</li>
 *   <li>Bean validation errors (invalid request data)</li>
 *   <li>Generic exceptions (unexpected errors)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles UserNotFoundException - returns HTTP 404 Not Found.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(
            UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles DuplicateEmailException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEmailException(
            DuplicateEmailException ex, HttpServletRequest request) {
        log.warn("Duplicate email error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles InvalidManagerException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(InvalidManagerException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidManagerException(
            InvalidManagerException ex, HttpServletRequest request) {
        log.warn("Invalid manager error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles UserDeletionException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(UserDeletionException.class)
    public ResponseEntity<ErrorResponseDto> handleUserDeletionException(
            UserDeletionException ex, HttpServletRequest request) {
        log.warn("User deletion error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles TemplateNotFoundException - returns HTTP 404 Not Found.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleTemplateNotFoundException(
            TemplateNotFoundException ex, HttpServletRequest request) {
        log.warn("Template not found: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles DuplicatePositionNameException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(DuplicatePositionNameException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicatePositionNameException(
            DuplicatePositionNameException ex, HttpServletRequest request) {
        log.warn("Duplicate position name error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles TemplateDeletionException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(TemplateDeletionException.class)
    public ResponseEntity<ErrorResponseDto> handleTemplateDeletionException(
            TemplateDeletionException ex, HttpServletRequest request) {
        log.warn("Template deletion error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles CsvImportException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(CsvImportException.class)
    public ResponseEntity<ErrorResponseDto> handleCsvImportException(
            CsvImportException ex, HttpServletRequest request) {
        log.warn("CSV import error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles TemplateTaskNotFoundException - returns HTTP 404 Not Found.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(TemplateTaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleTemplateTaskNotFoundException(
            TemplateTaskNotFoundException ex, HttpServletRequest request) {
        log.warn("Template task not found: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles OnboardingProcessNotFoundException - returns HTTP 404 Not Found.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(OnboardingProcessNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOnboardingProcessNotFoundException(
            OnboardingProcessNotFoundException ex, HttpServletRequest request) {
        log.warn("Onboarding process not found: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles OnboardingProcessDeletionException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(OnboardingProcessDeletionException.class)
    public ResponseEntity<ErrorResponseDto> handleOnboardingProcessDeletionException(
            OnboardingProcessDeletionException ex, HttpServletRequest request) {
        log.warn("Onboarding process deletion error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles OnboardingTaskNotFoundException - returns HTTP 404 Not Found.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 404 status
     */
    @ExceptionHandler(OnboardingTaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOnboardingTaskNotFoundException(
            OnboardingTaskNotFoundException ex, HttpServletRequest request) {
        log.warn("Onboarding task not found: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles InvalidCredentialsException - returns HTTP 401 Unauthorized.
     * 
     * <p>Thrown when user authentication fails due to invalid email or password.
     * For security reasons, the error message is intentionally generic to prevent
     * user enumeration attacks.</p>
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 401 status
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentialsException(
            InvalidCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed for request to: {}", request.getRequestURI());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Handles Bean Validation errors - returns HTTP 400 Bad Request with field-level errors.
     * 
     * <p>Triggered when {@code @Valid} annotation fails on request DTOs.
     * Returns detailed validation errors for each invalid field.</p>
     * 
     * @param ex the validation exception
     * @param request the HTTP request
     * @return error response with 400 status and validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error: {} field(s) failed validation", ex.getBindingResult().getErrorCount());
        
        List<ErrorResponseDto.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponseDto.ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());
        
        // Create summary message
        String message = "Validation failed for " + validationErrors.size() + " field(s)";
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles IllegalArgumentException - returns HTTP 400 Bad Request.
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument error: {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles all other unexpected exceptions - returns HTTP 500 Internal Server Error.
     * 
     * <p>Logs the full exception for debugging while returning a generic error message
     * to the client (avoiding exposure of sensitive implementation details).</p>
     * 
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponseDto error = new ErrorResponseDto(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred. Please contact support if the problem persists.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
