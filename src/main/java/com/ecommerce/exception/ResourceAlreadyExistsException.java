package com.ecommerce.exception;

/**
 * Custom exception class used to signal that a resource creation attempt has failed 
 * because the resource already exists in the system.
 * This is typically mapped to a 409 Conflict HTTP status code.
 * * Examples: Registering an email that is already in use, or creating a product 
 * with a duplicate SKU.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new ResourceAlreadyExistsException with a specific error message.
     * * @param message The detailed error message explaining which resource already exists.
     */
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}