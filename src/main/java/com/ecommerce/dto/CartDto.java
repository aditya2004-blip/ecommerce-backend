package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object representing the shopping cart. * This DTO is used to
 * send a summarized view of the user's cart to the frontend, including the list
 * of items and the pre-calculated total price to minimize client-side
 * computation.
 */

public class CartDto {
	private Long id;
	private List<CartItemDto> cartItems;
	private BigDecimal totalPrice;

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<CartItemDto> getItems() {
		return cartItems;
	}

	public void setItems(List<CartItemDto> cartItems) {
		this.cartItems = cartItems;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}
}