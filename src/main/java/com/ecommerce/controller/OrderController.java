package com.ecommerce.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for managing order-related operations including checkout, 
 * order history retrieval, status updates, and payment simulation.
 * * Security Configuration:
 * - CUSTOMER: Can only access, create, or modify their own orders (enforced via SpEL #userId == principal.id).
 * - ADMIN: Has global access to view or update any user's order history and statuses.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "5. Order API", description = "Endpoints for handling checkouts, payment simulations, and order tracking")
public class OrderController {

	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	private final OrderService orderService;
	private final ModelMapper modelMapper;

	/**
	 * Constructor-based dependency injection for service logic and object mapping.
	 */
	@Autowired
	public OrderController(OrderService orderService, ModelMapper modelMapper) {
		this.orderService = orderService;
		this.modelMapper = modelMapper;
	}

	/**
	 * Processes a checkout request by converting a user's active cart into a confirmed order.
	 * Secured using SpEL: Customers can only perform checkout for their own cart.
	 * * @param userId Unique identifier of the customer performing the checkout.
	 * @return ResponseEntity containing the newly created OrderDto and 201 Created status.
	 */
	@Operation(
		summary = "Checkout (Cart to Order)", 
		description = "Converts the current user's cart into a permanent Order record. **Security:** Restricted to the owner of the cart via SpEL.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping("/checkout")
	@PreAuthorize("hasAuthority('CUSTOMER') and #userId == principal.id")
	public ResponseEntity<OrderDto> checkout(@RequestParam Long userId) {
		logger.info("Received POST request at /api/orders/checkout to process checkout for User ID: {}", userId);

		Order order = orderService.checkout(userId);

		logger.info("Successfully processed checkout for User ID: {}. Assigned Order ID: {}. Returning 201 CREATED.",
				userId, order.getId());
		return new ResponseEntity<>(modelMapper.map(order, OrderDto.class), HttpStatus.CREATED);
	}
	
	/**
	 * Retrieves the full order history for a specific customer.
	 * Secured using SpEL: Admins have full access, Customers can only fetch their own order history.
	 * * @param userId Unique identifier of the user.
	 * @return ResponseEntity containing a list of OrderDto objects.
	 */
	@Operation(
		summary = "Get Order History", 
		description = "Returns all historical orders for a user. **Security:** Customers see only their own; Admins see all.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #userId == principal.id)")
	public ResponseEntity<List<OrderDto>> getOrderHistory(@RequestParam Long userId) {
		logger.info("Received GET request at /api/orders to fetch order history for User ID: {}", userId);

		List<Order> orders = orderService.getUserOrders(userId);

		List<OrderDto> orderDtos = orders.stream().map(order -> modelMapper.map(order, OrderDto.class))
				.collect(Collectors.toList());

		logger.info("Successfully retrieved and mapped {} orders for User ID: {}. Returning 200 OK.", orders.size(),
				userId);
		return ResponseEntity.ok(orderDtos);
	}

	/**
	 * Fetches detailed information for a single order by its ID.
	 * Secured using SpEL: Admins have full access, Customers can only view their own order details.
	 * * @param id Unique identifier of the order.
	 * @param userId Unique identifier of the user requesting the order.
	 * @return ResponseEntity containing the mapped OrderDto.
	 */
	@Operation(
		summary = "Get Order Details by ID", 
		description = "Fetches a specific order by ID. **Security:** Enforced via SpEL to prevent cross-user access.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #userId == principal.id)")
	public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id, @RequestParam Long userId) {
		logger.info("Received GET request at /api/orders/{} to fetch specific order details for User ID: {}", id, userId);

		Order order = orderService.getOrderById(id);

		logger.info("Successfully fetched details for Order ID: {}. Returning 200 OK.", id);
		return ResponseEntity.ok(modelMapper.map(order, OrderDto.class));
	}

	/**
	 * Updates the status of an order (e.g., SHIPPED, DELIVERED).
	 * Restricted strictly to administrative users only.
	 * * @param id Unique identifier of the order to be updated.
	 * @param status The new OrderStatus to be applied.
	 * @return ResponseEntity containing the updated OrderDto.
	 */
	@Operation(
		summary = "Update Order Status", 
		description = "Allows updating the fulfillment state (e.g., SHIPPED). **Security:** Strictly limited to ADMIN role.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PutMapping("/{id}/status")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
		logger.info("Received PUT request at /api/orders/{}/status to update status to: {}", id, status);

		Order updatedOrder = orderService.updateOrderStatus(id, status);

		logger.info("Successfully updated Order ID: {} to status: {}. Returning 200 OK.", id, status);
		return ResponseEntity.ok(modelMapper.map(updatedOrder, OrderDto.class));
	}

	/**
	 * Simulates a payment gateway response to finalize the order process.
	 * Secured using SpEL: Customers can only simulate payments for their own orders.
	 * * @param id Unique identifier of the order being paid for.
	 * @param userId Unique identifier of the user attempting the payment.
	 * @param success Boolean flag indicating whether the simulated payment passed or failed.
	 * @return ResponseEntity containing the OrderDto with updated payment and order status.
	 */
	@Operation(
		summary = "Simulate Payment", 
		description = "Triggers the payment finalization logic. **Security:** Restricted to the order owner.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping("/{id}/pay")
	@PreAuthorize("hasAuthority('CUSTOMER') and #userId == principal.id")
	public ResponseEntity<OrderDto> simulatePayment(@PathVariable Long id, @RequestParam Long userId, @RequestParam boolean success) {
		logger.info("Received POST request at /api/orders/{}/pay simulating payment success: {} for User ID: {}", id, success, userId);

		Order updatedOrder = orderService.simulatePayment(id, success);

		logger.info("Successfully processed simulated payment for Order ID: {}. Returning 200 OK.", id);
		return ResponseEntity.ok(modelMapper.map(updatedOrder, OrderDto.class));
	}
}