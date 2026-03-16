package com.ecommerce.exception;

/**
 * Custom exception class used to signal that a requested resource could not be found.
 * This exception is typically intercepted by the GlobalExceptionHandler to return 
 * a 404 Not Found HTTP status code to the client.
 * * Examples: Fetching a product by a non-existent ID or searching for a user 
 * email that is not registered in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * * @param message The descriptive error message explaining which resource was missing.
     */
	public ResourceNotFoundException(String message) {
        super(message);
    }
}