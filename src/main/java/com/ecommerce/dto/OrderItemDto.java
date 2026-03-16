package com.ecommerce.dto;

/**
 * Data Transfer Object representing a single line item within a finalized order.
 * * It captures the state of the product (via ProductDto) and its price at 
 * the specific moment the order was placed, ensuring historical accuracy 
 * even if product details change later in the catalog.
 */

public class OrderItemDto {
	private Long id;
	private ProductDto product;// Nested DTO to provide structured product information
	private Integer quantity;
	private Double price;

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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
}
