package com.ecommerce.serviceImplTest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.EmailService;
import com.ecommerce.serviceImpl.OrderServiceImpl;
import com.ecommerce.serviceImpl.RedisInventoryServiceImpl;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CartService cartService;

	@Mock
	private EmailService emailService;

	@Mock
	private RedisInventoryServiceImpl redisInventoryService;

	@InjectMocks
	private OrderServiceImpl orderService;

	private User mockUser;
	private Cart mockCart;
	private Product mockProduct;
	private CartItem mockCartItem;
	private Order mockOrder;

	@BeforeEach
	void setUp() {
		mockUser = new User();
		mockUser.setId(1L);
		mockUser.setEmail("adityapati2004@gmail.com");

		mockProduct = new Product();
		mockProduct.setId(100L);
		mockProduct.setName("Gaming Keyboard");
		mockProduct.setPrice(new BigDecimal("100.00"));
		mockProduct.setStock(10);

		mockCartItem = new CartItem();
		mockCartItem.setId(1000L);
		mockCartItem.setProduct(mockProduct);
		mockCartItem.setQuantity(2);
		mockCartItem.setPrice(new BigDecimal("100.00"));

		mockCart = new Cart();
		mockCart.setId(10L);
		mockCart.setUser(mockUser);
		mockCart.setCartItems(new ArrayList<>(List.of(mockCartItem)));
		mockCart.setTotalPrice(new BigDecimal("200.00"));

		mockOrder = new Order();
		mockOrder.setId(500L);
		mockOrder.setUser(mockUser);
		mockOrder.setOrderStatus(OrderStatus.PLACED);
		mockOrder.setPaymentStatus(PaymentStatus.PENDING);
		mockOrder.setTotalAmount(new BigDecimal("200.00"));
	}

	@DisplayName("Checkout - Should successfully create order, deduct stock, and clear cart")
	@Test
	void testCheckout_Success() {
		when(cartService.getCartByUserId(1L)).thenReturn(mockCart);
		
		when(redisInventoryService.reserveStock(100L, 2)).thenReturn(true);
		
		when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

		Order savedOrder = orderService.checkout(1L);

		assertNotNull(savedOrder);
		assertEquals(OrderStatus.PLACED, savedOrder.getOrderStatus());
		assertEquals(PaymentStatus.PENDING, savedOrder.getPaymentStatus());

		assertEquals(8, mockProduct.getStock(), "Product stock should be deducted");

		verify(redisInventoryService, times(1)).reserveStock(100L, 2);
		
		verify(productRepository, times(1)).save(mockProduct);
		verify(orderRepository, times(1)).save(any(Order.class));
		verify(cartService, times(1)).clearCart(mockCart);
		verify(emailService, times(1)).sendOrderConfirmation(eq("adityapati2004@gmail.com"), anyLong(),
				any(BigDecimal.class));

	}

	@DisplayName("Checkout - Should throw exception if cart is completely empty")
	@Test
	void testCheckout_EmptyCart_ThrowsException() {
		mockCart.getCartItems().clear();
		when(cartService.getCartByUserId(1L)).thenReturn(mockCart);

		assertThrows(BadRequestException.class, () -> {
			orderService.checkout(1L);
		});

		verify(orderRepository, never()).save(any(Order.class));
		verify(cartService, never()).clearCart(any(Cart.class));
		verify(emailService, never()).sendOrderConfirmation(anyString(), anyLong(), any());
	}

	@DisplayName("Checkout - Should throw exception if product stock is lower than requested quantity")
	@Test
	void testCheckout_InsufficientStock_ThrowsException() {
		mockProduct.setStock(1);
		when(cartService.getCartByUserId(1L)).thenReturn(mockCart);
		
		when(redisInventoryService.reserveStock(100L, 2)).thenReturn(false);

		assertThrows(BadRequestException.class, () -> {
			orderService.checkout(1L);
		});

		verify(redisInventoryService, times(1)).reserveStock(100L, 2);
		
		verify(productRepository, never()).save(any(Product.class));
		verify(orderRepository, never()).save(any(Order.class));
		verify(emailService, never()).sendOrderConfirmation(anyString(), anyLong(), any());
	}

	@DisplayName("Get User Orders - Should return list of orders for a valid user")
	@Test
	void testGetUserOrders_Success() {
		when(orderRepository.findByUserId(1L)).thenReturn(List.of(mockOrder));

		List<Order> orders = orderService.getUserOrders(1L);

		assertFalse(orders.isEmpty());
		assertEquals(1, orders.size());
		verify(orderRepository, times(1)).findByUserId(1L);
	}

	@DisplayName("Get Order By ID - Should return order when ID exists")
	@Test
	void testGetOrderById_Success() {
		when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

		Order order = orderService.getOrderById(500L);

		assertNotNull(order);
		assertEquals(500L, order.getId());
	}

	@DisplayName("Get Order By ID - Should throw exception when ID does not exist")
	@Test
	void testGetOrderById_NotFound_ThrowsException() {
		when(orderRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			orderService.getOrderById(999L);
		});
	}

	@DisplayName("Update Order Status - Should successfully update and save new status")
	@Test
	void testUpdateOrderStatus_Success() {
		when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));
		when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

		Order updatedOrder = orderService.updateOrderStatus(500L, OrderStatus.SHIPPED);

		assertEquals(OrderStatus.SHIPPED, updatedOrder.getOrderStatus());
		verify(orderRepository, times(1)).save(mockOrder);
	}

	@DisplayName("Simulate Payment - Should set status to SUCCESS when payment passes")
	@Test
	void testSimulatePayment_Success() {
		when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));
		when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

		Order updatedOrder = orderService.simulatePayment(500L, true);

		assertEquals(PaymentStatus.SUCCESS, updatedOrder.getPaymentStatus());
		assertEquals(OrderStatus.PLACED, updatedOrder.getOrderStatus(), "Order status should remain PLACED on success");
	}

	@DisplayName("Simulate Payment - Should set status to FAILED and CANCELLED when payment fails")
	@Test
	void testSimulatePayment_Failure() {
		when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));
		when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

		Order updatedOrder = orderService.simulatePayment(500L, false);

		assertEquals(PaymentStatus.FAILED, updatedOrder.getPaymentStatus());
		assertEquals(OrderStatus.CANCELLED, updatedOrder.getOrderStatus());
	}

}