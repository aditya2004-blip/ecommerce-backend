package com.ecommerce.controller;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.dto.CartDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for managing the Shopping Cart operations.
 * * Security Configuration:
 * - CUSTOMER: Can only access and modify their own cart (enforced via SpEL #userId == principal.id).
 * - ADMIN: Has global access to view, update, or delete any user's cart. This is essential for 
 * customer support, troubleshooting, and managing abandoned carts.
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "4. Shopping Cart API", description = "Endpoints for managing customer shopping carts and items")
public class CartController {

	private static final Logger logger = LoggerFactory.getLogger(CartController.class);

	private final CartService cartService;
	private final ModelMapper modelMapper;
	
	/**
	 * Constructor injection for Cart business logic and DTO mapping utilities.
	 */
	@Autowired
	public CartController(CartService cartService, ModelMapper modelMapper) {
		this.cartService = cartService;
		this.modelMapper = modelMapper;
	}

	/**
	 * Retrieves the current shopping cart for a specific user.
	 * Secured using SpEL: Admins have full access, Customers can only fetch their own cart.
	 * * @param userId Unique identifier of the user whose cart is to be fetched.
	 * @return ResponseEntity containing the CartDto with current items and total price.
	 */
	@Operation(
		summary = "Get Cart by User ID", 
		description = "Fetches the shopping cart and all contained items. **Security:** Customers can only fetch their own cart via ID matching; Admins have global access.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #userId == principal.id)")
	public ResponseEntity<CartDto> getCart(@RequestParam Long userId) {
		logger.info("Received GET request at /api/cart to fetch cart for User ID: {}", userId);

		Cart cart = cartService.getCartByUserId(userId);
		CartDto cartDto = modelMapper.map(cart, CartDto.class);

		logger.info("Successfully fetched and mapped cart for User ID: {}. Returning 200 OK.", userId);
		return ResponseEntity.ok(cartDto);
	}

	/**
	 * Adds a product to the user's cart or updates the quantity if the product already exists.
	 * Secured using SpEL: Admins have full access, Customers can only modify their own cart.
	 * * @param userId Unique identifier of the user.
	 * @param productId Unique identifier of the product to be added.
	 * @param quantity The amount of the product to be placed in the cart.
	 * @return ResponseEntity containing the updated CartDto.
	 */
	@Operation(
		summary = "Add Product to Cart", 
		description = "Adds a product to the cart or increments quantity if it exists. **Security:** Enforces user isolation via SpEL.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping("/add/{productId}")
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #userId == principal.id)")
	public ResponseEntity<CartDto> addToCart(@RequestParam Long userId, @PathVariable Long productId,
			@RequestParam Integer quantity) {
		logger.info("Received POST request at /api/cart/add/{} for User ID: {}. Requested Quantity: {}", productId,
				userId, quantity);

		Cart cart = cartService.addOrUpdateCartItem(userId, productId, quantity);
		CartDto cartDto = modelMapper.map(cart, CartDto.class);

		logger.info("Successfully added Product ID: {} to cart for User ID: {}. Returning 200 OK.", productId, userId);
		return ResponseEntity.ok(cartDto);
	}

	/**
	 * Updates the quantity of an existing item within the shopping cart.
	 * Secured using SpEL: Admins have full access, Customers can only update their own cart.
	 * * @param userId Unique identifier of the user.
	 * @param productId Unique identifier of the product to update.
	 * @param quantity The new quantity to be set for the product.
	 * @return ResponseEntity containing the updated Cart entity.
	 */
	@Operation(
		summary = "Update Cart Item Quantity", 
		description = "Modifies the quantity of a specific item already in the cart. **Security:** Restricted to the owner or an Admin.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PutMapping("/update/{productId}")
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #userId == principal.id)")
	public ResponseEntity<Cart> updateCartItem(@RequestParam Long userId, @PathVariable Long productId,
			@RequestParam Integer quantity) {
		logger.info("Received PUT request at /api/cart/update/{} for User ID: {}. New Quantity: {}", 
                productId, userId, quantity);
        
        Cart updatedCart = cartService.addOrUpdateCartItem(userId, productId, quantity);
        
        logger.info("Successfully updated Product ID: {} in cart for User ID: {}. Returning 200 OK.", productId, userId);
        return ResponseEntity.ok(updatedCart);
	}

	/**
	 * Completely removes a specific product from the user's shopping cart.
	 * Secured using SpEL: Admins have full access, Customers can only remove items from their own cart.
	 * * @param userId Unique identifier of the user.
	 * @param productId Unique identifier of the product to be removed.
	 * @return ResponseEntity containing the updated Cart entity after deletion.
	 */
	@Operation(
		summary = "Remove Product from Cart", 
		description = "Deletes a specific product line-item from the cart. **Security:** Owners/Admins only.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@DeleteMapping("/remove/{productId}")
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #userId == principal.id)")
	public ResponseEntity<Cart> removeFromCart(@RequestParam Long userId, @PathVariable Long productId) {
		logger.info("Received DELETE request at /api/cart/remove/{} for User ID: {}", productId, userId);
        
        Cart updatedCart = cartService.removeProductFromCart(userId, productId);
        
        logger.info("Successfully removed Product ID: {} from cart for User ID: {}. Returning 200 OK.", productId, userId);
        return ResponseEntity.ok(updatedCart);
	}
}