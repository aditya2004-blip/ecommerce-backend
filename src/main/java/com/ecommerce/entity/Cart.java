package com.ecommerce.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Persistence entity representing a User's Shopping Cart. * This entity acts as
 * a container for CartItems and maintains a 1-to-1 relationship with the User.
 * It uses JPA annotations to map to the 'carts' table in the database.
 */

@Entity
@Table(name = "carts")
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The owner of the cart. Uses @JsonIgnore to prevent infinite recursion during
	 * JSON serialization and LAZY fetching to optimize performance.
	 */

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "total_price", precision = 10, scale = 2)
	private BigDecimal totalPrice = BigDecimal.ZERO;

	/**
	 * List of items currently held in the cart. * cascade = CascadeType.ALL:
	 * Operations on the cart (like delete) propagate to items. orphanRemoval =
	 * true: Removing an item from this list deletes it from the database.
	 */

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> cartItems = new ArrayList<>();

	public Cart() {
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public List<CartItem> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}
}