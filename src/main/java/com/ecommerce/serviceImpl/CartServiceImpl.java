package com.ecommerce.serviceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.ProductService;

/**
 * Implementation of the {@link CartService} providing shopping cart business logic.
 * This class manages the persistence of cart items, stock validation, and 
 * the calculation of total cart values within a transactional context.
 */
@Service
public class CartServiceImpl implements CartService {

	private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

	private final CartRepository cartRepository;
	private final ProductService productService;

	/**
	 * Constructor-based injection for repository and product services.
	 */
	@Autowired
	public CartServiceImpl(CartRepository cartRepository, ProductService productService) {
		this.cartRepository = cartRepository;
		this.productService = productService;
	}

	/**
	 * Fetches the user's cart from the database.
	 * * @param userId Unique identifier of the user.
	 * @return The associated Cart entity.
	 * @throws ResourceNotFoundException if the cart does not exist for the specified user.
	 */
	@Override
	public Cart getCartByUserId(Long userId) {
		logger.debug("Fetching cart from database for user ID: {}", userId);
		return cartRepository.findByUserId(userId).orElseThrow(() -> {
			logger.error("Cart retrieval failed. No cart exists for user ID: {}", userId);
			return new ResourceNotFoundException("Cart Not Found for user ID: " + userId);
		});
	}

	/**
	 * Adds a product to the cart or modifies the quantity of an existing item.
	 * Includes critical stock verification to ensure availability before updating.
	 * * @param userId ID of the user owning the cart.
	 * @param productId ID of the product to add/update.
	 * @param quantity The target quantity for the item.
	 * @return The saved Cart entity with updated items and total price.
	 * @throws BadRequestException if the requested quantity exceeds available stock.
	 */
	@Override
	@Transactional
	public Cart addOrUpdateCartItem(Long userId, Long productId, Integer quantity) {

		logger.info("User ID: {} is attempting to add/update Product ID: {} with quantity: {}", userId, productId,
				quantity);

		Cart cart = getCartByUserId(userId);
		Product product = productService.getProductById(productId);

		// Inventory Guard: Prevent ordering more than current stock levels
		if (product.getStock() < quantity) {
			logger.warn(
					"Cart update failed for User ID: {}. Requested quantity {}, but only {} in stock for Product ID: {}",
					userId, quantity, product.getStock(), productId);
			throw new BadRequestException("Not enough stock available. Current stock: " + product.getStock());
		}

		// Identify if the product is already in the cart
		Optional<CartItem> existingItem = cart.getCartItems().stream()
				.filter(item -> item.getProduct().getId().equals(productId)).findFirst();

		if (existingItem.isPresent()) {
			logger.debug("Product ID: {} already exists in cart. Updating quantity to: {}", productId, quantity);
			existingItem.get().setQuantity(quantity);
		} else {
			logger.debug("Product ID: {} is new to the cart. Creating new CartItem.", productId);
			CartItem newItem = new CartItem();
			newItem.setCart(cart);
			newItem.setProduct(product);
			newItem.setQuantity(quantity);
			newItem.setPrice(product.getPrice());
			cart.getCartItems().add(newItem);
		}
		
		// Refresh total price before persistence
		recalculateTotal(cart);

		Cart savedCart = cartRepository.save(cart);

		logger.info("Successfully updated cart for User ID: {}. New cart total: {}", userId, savedCart.getTotalPrice());
		return savedCart;
	}

	/**
	 * Removes a specific product from the cart and updates the total price.
	 * * @param userId ID of the user owning the cart.
	 * @param productId ID of the product to be purged.
	 * @return The updated Cart entity.
	 */
	@Override
	@Transactional
	public Cart removeProductFromCart(Long userId, Long productId) {

		logger.info("User ID: {} is attempting to remove Product ID: {} from their cart.", userId, productId);

		Cart cart = getCartByUserId(userId);

		// Remove the item matching the product ID from the collection
		boolean isRemoved = cart.getCartItems().removeIf(item -> item.getProduct().getId().equals(productId));

		if (isRemoved) {
			logger.debug("Product ID: {} was successfully found and removed from the cart.", productId);
		} else {
			logger.warn("Attempted to remove Product ID: {} but it was not found in the cart for User ID: {}",
					productId, userId);
		}

		recalculateTotal(cart);
		Cart savedCart = cartRepository.save(cart);

		logger.info("Cart update complete after removal for User ID: {}. New cart total: {}", userId,
				savedCart.getTotalPrice());
		return savedCart;
	}

	/**
	 * Empties all items from the cart and resets the total price to zero.
	 * Usually triggered after a successful order placement.
	 * * @param cart The Cart entity to be cleared.
	 */
	@Override
	public void clearCart(Cart cart) {
		logger.info("Initiating clear cart operation for cart ID: {}", cart.getId());

		cart.getCartItems().clear();
		cart.setTotalPrice(BigDecimal.ZERO);
		cartRepository.save(cart);

		logger.info("Successfully cleared cart items and reset total price to zero for cart ID: {}", cart.getId());
	}

	/**
	 * Internal utility to sum up the price of all items currently in the cart.
	 * This ensures the totalPrice attribute is always synchronized with the item collection.
	 * * @param cart The cart to calculate.
	 */
	private void recalculateTotal(Cart cart) {
		logger.debug("Recalculating total price for cart ID: {}", cart.getId());

		BigDecimal total = BigDecimal.ZERO;
		for (CartItem item : cart.getCartItems()) {
			// Multiply unit price by quantity for each line item
			total = total.add(item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())));
		}
		cart.setTotalPrice(total);

		logger.debug("Recalculation complete. New total price: {}", total);
	}
}