package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Persistence entity representing an individual item within a Shopping Cart. *
 * This class links a specific Product to a Cart and records the quantity and
 * price. It serves as the bridge in the Many-to-One relationship between carts
 * and products.
 */

@Entity
@Table(name = "cart_items")
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The parent cart this item belongs to.
	 * 
	 * @JsonIgnore is used to prevent infinite recursion during JSON serialization,
	 *             as the Cart entity already references its list of CartItems.
	 */

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	/**
	 * The product associated with this cart entry. Fetched lazily for performance;
	 * visibility is maintained for DTO mapping.
	 */

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	public CartItem() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Cart getCart() {
		return cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	// Getters and setters for the new price field
	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}