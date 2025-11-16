package com.narciarz.benew.exceptions;

/**
 * Exception thrown when user authentication fails due to invalid credentials.
 * 
 * <p>This exception is thrown by the authentication service when:</p>
 * <ul>
 *   <li>User email does not exist in the system</li>
 *   <li>Provided password does not match stored password hash</li>
 * </ul>
 * 
 * <p>Mapped to HTTP 401 Unauthorized by {@link GlobalExceptionHandler}.</p>
 * 
 * <p>Security note: For security reasons, the error message should not reveal
 * whether the email exists or the password is wrong. Use a generic message like
 * "Invalid email or password" to prevent user enumeration attacks.</p>
 */
public class InvalidCredentialsException extends RuntimeException {
    
    /**
     * Constructs a new InvalidCredentialsException with a generic error message.
     * 
     * <p>Default message: "Invalid email or password"</p>
     */
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
    
    /**
     * Constructs a new InvalidCredentialsException with a custom message.
     * 
     * @param message the detail message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new InvalidCredentialsException with a custom message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

