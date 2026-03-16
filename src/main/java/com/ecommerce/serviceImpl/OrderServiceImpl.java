package com.ecommerce.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.EmailService;
import com.ecommerce.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final CartService cartService;
	private final EmailService emailService;
	private final RedisInventoryServiceImpl redisInventoryService;

	@Autowired
	public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository,
			CartService cartService, EmailService emailService, RedisInventoryServiceImpl redisInventoryService) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.cartService = cartService;
		this.emailService = emailService;
		this.redisInventoryService = redisInventoryService;
	}

	@Override
	@Transactional
	public Order checkout(Long userId) {

		logger.info("Initiating checkout process for user ID: {}", userId);

		Cart cart = cartService.getCartByUserId(userId);
		logger.debug("Cart retrieved for user ID: {}. Number of items: {}, Total Price: {}", userId,
				cart.getCartItems().size(), cart.getTotalPrice());

		if (cart.getCartItems().isEmpty()) {
			logger.warn("Checkout failed. Cart is empty for user ID: {}", userId);
			throw new BadRequestException("Cart is empty. Cannot proceed to checkout.");
		}
		Order order = new Order();
		order.setUser(cart.getUser());
		order.setTotalAmount(cart.getTotalPrice());
		order.setOrderStatus(OrderStatus.PLACED);
		order.setPaymentStatus(PaymentStatus.PENDING);
		order.setOrderDate(LocalDateTime.now());

		logger.debug("Processing {} items from the cart for order creation.", cart.getCartItems().size());

		for (CartItem cartItem : cart.getCartItems()) {
			Product product = cartItem.getProduct();

			boolean isReserved = redisInventoryService.reserveStock(product.getId(), cartItem.getQuantity());

			if (!isReserved) {
				logger.warn("Checkout failed. Insufficient stock in Redis for product: {}", product.getName());
				throw new BadRequestException("Product out of stock due to high demand: " + product.getName());
			}
			
			
			if (product.getStock() < cartItem.getQuantity()) {
				logger.warn(
						"Checkout failed for user ID: {}. Insufficient stock for product ID: {}. Requested: {}, Available: {}",
						userId, product.getId(), cartItem.getQuantity(), product.getStock());
				throw new BadRequestException("Product out of stock or insufficient quantity: " + product.getName());
			}

			logger.debug("Deducting {} units from product ID: {}", cartItem.getQuantity(), product.getId());
			product.setStock(product.getStock() - cartItem.getQuantity());
			productRepository.save(product);

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProduct(product);
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setPrice(product.getPrice());

			order.getOrderItems().add(orderItem);
		}
		Order savedOrder = orderRepository.save(order);

		logger.info("Order ID: {} successfully created for user ID: {}. Total amount: {}", savedOrder.getId(), userId,
				savedOrder.getTotalAmount());

		logger.debug("Clearing cart for user ID: {} post-checkout.", userId);

		cartService.clearCart(cart);
		emailService.sendOrderConfirmation(savedOrder.getUser().getEmail(), savedOrder.getId(),
				savedOrder.getTotalAmount());
		return savedOrder;
	}

	@Override
	public List<Order> getUserOrders(Long userId) {
		logger.debug("Fetching all orders for user ID: {}", userId);
		List<Order> orders = orderRepository.findByUserId(userId);
		logger.debug("Found {} orders for user ID: {}", orders.size(), userId);
		return orders;
	}

	@Override
	public Order getOrderById(Long id) {
		logger.debug("Fetching order by ID: {}", id);
		return orderRepository.findById(id).orElseThrow(() -> {
			logger.error("Failed to fetch order. No order found with ID: {}", id);
			return new ResourceNotFoundException("Order not found with id: " + id);
		});
	}

	@Override
	public Order updateOrderStatus(Long orderId, OrderStatus status) {
		logger.info("Attempting to update status for order ID: {} to {}", orderId, status);

		Order order = getOrderById(orderId);
		order.setOrderStatus(status);
		Order updatedOrder = orderRepository.save(order);
		logger.info("Successfully updated order ID: {} to status: {}", orderId, status);
		return updatedOrder;
	}

	@Override
	public Order simulatePayment(Long orderId, boolean success) {
		logger.info("Simulating payment for order ID: {}. Payment successful: {}", orderId, success);

		Order order = getOrderById(orderId);

		if (success) {
			order.setPaymentStatus(PaymentStatus.SUCCESS);
			logger.info("Payment successful for order ID: {}. PaymentStatus updated to SUCCESS.", orderId);
		} else {
			order.setPaymentStatus(PaymentStatus.FAILED);
			order.setOrderStatus(OrderStatus.CANCELLED);
			logger.warn("Payment failed for order ID: {}. OrderStatus marked as CANCELLED.", orderId);
		}
		return orderRepository.save(order);
	}

}
