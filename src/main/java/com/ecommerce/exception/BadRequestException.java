package com.ecommerce.exception;

/**
 * Custom exception class used to handle invalid client requests.
 * This exception is typically thrown when the incoming request data 
 * fails business logic validation (e.g., insufficient stock, invalid quantities).
 * * Extends {@link RuntimeException} to allow for unchecked exception handling 
 * within the Spring Boot framework.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Constructs a new BadRequestException with the specified detail message.
     * * @param message The descriptive error message to be returned to the client.
     */
    public BadRequestException(String message) {
        super(message);
    }
}