package com.narciarz.benew.exceptions;

/**
 * Exception thrown when CSV import validation or processing fails.
 * 
 * <p>This exception is used to handle various CSV import errors including:</p>
 * <ul>
 *   <li>Invalid file format or missing file</li>
 *   <li>Missing required columns in CSV</li>
 *   <li>Invalid data types or values</li>
 *   <li>CSV parsing errors</li>
 * </ul>
 * 
 * <p>Results in HTTP 400 Bad Request response.</p>
 */
public class CsvImportException extends RuntimeException {
    
    /**
     * Constructs a new CsvImportException with the specified detail message.
     * 
     * @param message the detail message explaining why import failed
     */
    public CsvImportException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new CsvImportException with the specified detail message and cause.
     * 
     * @param message the detail message explaining why import failed
     * @param cause the underlying cause of the exception
     */
    public CsvImportException(String message, Throwable cause) {
        super(message, cause);
    }
}

