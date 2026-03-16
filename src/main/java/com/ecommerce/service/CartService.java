package com.ecommerce.service;

import com.ecommerce.entity.Cart;

/**
 * Service interface defining the business logic for shopping cart operations.
 * This service manages the lifecycle of a user's cart, including item persistence,
 * quantity updates, and synchronization between products and the user's active session.
 */
public interface CartService {

    /**
     * Retrieves the shopping cart associated with a specific user.
     * If no cart exists for the given user, the implementation should handle 
     * the initialization or retrieval logic accordingly.
     * * @param userId The unique identifier of the user.
     * @return The Cart entity belonging to the user.
     * @throws com.ecommerce.exception.ResourceNotFoundException if the user or cart is not found.
     */
	Cart getCartByUserId(Long userId);

    /**
     * Adds a product to the cart or updates the quantity of an existing item.
     * This method ensures that the cart state is consistent with current inventory 
     * and handles the calculations for line item totals.
     * * @param userId The ID of the user performing the action.
     * @param productId The ID of the product to add/update.
     * @param quantity The desired quantity (positive value).
     * @return The updated Cart entity.
     * @throws com.ecommerce.exception.BadRequestException if inventory is insufficient or quantity is invalid.
     */
	Cart addOrUpdateCartItem(Long userId, Long productId, Integer quantity);

    /**
     * Removes a specific product entirely from the user's shopping cart.
     * * @param userId The ID of the user owning the cart.
     * @param productId The ID of the product to be removed.
     * @return The updated Cart entity after the removal.
     */
    Cart removeProductFromCart(Long userId, Long productId);

    /**
     * Clears all items from the specified cart.
     * Typically invoked after a successful checkout process or upon manual request.
     * * @param cart The Cart entity to be emptied.
     */
    void clearCart(Cart cart);
}