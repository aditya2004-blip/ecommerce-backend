package com.ecommerce.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

/**
 * Persistence entity representing a finalized customer Order. * This class
 * serves as the primary record for transactions, tracking the lifecycle of a
 * purchase through both payment and fulfillment statuses.
 */

@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Optimistic locking version field. Prevents concurrent modification issues
	 * (Lost Updates) during high-traffic status changes or payment processing.
	 */

	@Version
	private Long version;

	/**
	 * The user who placed the order. Linked via a Many-to-One
	 * relationship. @JsonIgnore prevents infinite recursion in API responses.
	 */

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/**
	 * The total monetary value of the order. Precision of 10 and scale of 2 ensures
	 * exact financial decimal storage.
	 */

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	/**
	 * Automatic timestamp of when the order was created in the database.
	 */

	@CreationTimestamp
	@Column(name = "order_date", updatable = false)
	private LocalDateTime orderDate;

	/**
	 * Current state of the transaction (e.g., PENDING, COMPLETED, FAILED). Stored
	 * as a String in the database for better readability.
	 */

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false)
	private PaymentStatus paymentStatus;

	/**
	 * Current state of order fulfillment (e.g., PLACED, SHIPPED, DELIVERED). Stored
	 * as a String in the database.
	 */

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status", nullable = false)
	private OrderStatus orderStatus;

	/**
	 * List of individual line items associated with this specific order. * cascade
	 * = CascadeType.ALL: Operations on the order affect all items. orphanRemoval =
	 * true: Removing an item from this list deletes it from the database.
	 */

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems = new ArrayList<>();

	public Order() {
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}
}
