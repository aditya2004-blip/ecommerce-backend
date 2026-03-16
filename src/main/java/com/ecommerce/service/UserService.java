package com.ecommerce.service;

import com.ecommerce.entity.User;

/**
 * Service interface for managing user profile and account information.
 * This interface provides the contract for retrieving, modifying, and 
 * removing user records from the system after the initial authentication process.
 */
public interface UserService {

    /**
     * Retrieves detailed information for a specific user.
     * * @param id The unique identifier of the user record.
     * @return The User entity matching the provided identifier.
     * @throws com.ecommerce.exception.ResourceNotFoundException if no user exists with the given ID.
     */
    User getUserById(Long id);

    /**
     * Updates an existing user's profile information.
     * Typically used for updating personal details like names or contact information 
     * while maintaining the integrity of sensitive fields like passwords and roles.
     * * @param id The ID of the user to be updated.
     * @param updatedDetails An entity containing the revised user information.
     * @return The updated User entity as persisted in the database.
     */
    User updateUser(Long id, User updatedDetails);

    /**
     * Permanently deletes a user account from the system.
     * Implementations should ensure that associated resources (like active carts) 
     * are handled according to the system's data retention policy.
     * * @param id The unique identifier of the user to be removed.
     * @throws com.ecommerce.exception.ResourceNotFoundException if the user is not found.
     */
    void deleteUser(Long id);
}