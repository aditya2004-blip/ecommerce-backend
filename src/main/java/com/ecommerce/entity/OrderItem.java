package com.ecommerce.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Persistence entity representing a specific line item within a finalized
 * Order. * Unlike a CartItem, which represents a transient state, the OrderItem
 * serves as a permanent historical record of a purchase, capturing the product
 * price and quantity at the exact moment the transaction was completed.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

	/**
	 * Unique identifier for the order item record.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The parent order to which this line item belongs. Uses @JsonIgnore to prevent
	 * circular references during JSON serialization.
	 */
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	/**
	 * The product associated with this order entry. Access is set to LAZY to
	 * optimize database performance unless explicitly accessed.
	 */
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	/**
	 * The number of units purchased.
	 */
	@Column(nullable = false)
	private Integer quantity;

	/**
	 * The unit price of the product at the time of purchase. Stored as BigDecimal
	 * to maintain financial precision and protect against future price changes in
	 * the product catalog.
	 */
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	/**
	 * Default constructor for JPA.
	 */
	public OrderItem() {
	}

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}