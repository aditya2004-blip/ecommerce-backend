package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object representing an individual entry within a shopping cart.
 * * Maps a specific product to its requested quantity and the calculated price
 * at the time of addition. This DTO utilizes ProductDto to ensure consistent
 * and safe product information exposure.
 */

public class CartItemDto {
	private Long id;
	private ProductDto product; // Uses ProductDto to keep it clean
	private Integer quantity;
	private BigDecimal price;

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProductDto getProduct() {
		return product;
	}

	public void setProduct(ProductDto product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}