package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;

/**
 * Unit test suite for the {@link OrderController}.
 * This class validates the order processing endpoints, including checkout logic,
 * order history retrieval, status updates, and payment simulations.
 * It ensures proper status codes and DTO mapping using MockMvc.
 */
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private ModelMapper modelMapper;

    /**
     * The controller under test with mocked dependencies injected.
     */
    @InjectMocks
    private OrderController orderController;

    private Order mockOrder;
    private OrderDto mockOrderDto;

    /**
     * Initializes the standalone MockMvc environment and common test entities.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

        mockOrder = new Order();
        mockOrder.setId(500L);
        mockOrder.setTotalAmount(new BigDecimal("250.00"));
        mockOrder.setOrderStatus(OrderStatus.PLACED);

        mockOrderDto = new OrderDto();
        mockOrderDto.setId(500L);
        mockOrderDto.setTotalAmount(new BigDecimal("250.00"));
    }

    /**
     * Verifies the checkout endpoint.
     * Checks for a 201 Created status and ensures the cart conversion to order is successful.
     */
    @DisplayName("POST /api/orders/checkout - Should return 201 Created and mapped OrderDto")
    @Test
    void testCheckout_Success() throws Exception {
        when(orderService.checkout(1L)).thenReturn(mockOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDto.class))).thenReturn(mockOrderDto);

        mockMvc.perform(post("/api/orders/checkout")
                .param("userId", "1") 
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.totalAmount").value(250.00));

        verify(orderService, times(1)).checkout(1L);
    }

    /**
     * Verifies the order history retrieval for a specific user.
     * Ensures the response body is a list containing the expected order IDs.
     */
    @DisplayName("GET /api/orders - Should return 200 OK and a List of OrderDtos")
    @Test
    void testGetOrderHistory_Success() throws Exception {
        when(orderService.getUserOrders(1L)).thenReturn(List.of(mockOrder));
        when(modelMapper.map(any(Order.class), eq(OrderDto.class))).thenReturn(mockOrderDto);

        mockMvc.perform(get("/api/orders")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(500L)); 

        verify(orderService, times(1)).getUserOrders(1L);
    }

    /**
     * Verifies fetching a single order detail by ID.
     */
    @DisplayName("GET /api/orders/{id} - Should return 200 OK and mapped OrderDto")
    @Test
    void testGetOrderById_Success() throws Exception {
        when(orderService.getOrderById(500L)).thenReturn(mockOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDto.class))).thenReturn(mockOrderDto);

        mockMvc.perform(get("/api/orders/500")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L));

        verify(orderService, times(1)).getOrderById(500L);
    }

    /**
     * Verifies the administrative endpoint for updating order statuses.
     */
    @DisplayName("PUT /api/orders/{id}/status - Should return 200 OK and mapped OrderDto")
    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        when(orderService.updateOrderStatus(500L, OrderStatus.SHIPPED)).thenReturn(mockOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDto.class))).thenReturn(mockOrderDto);

        mockMvc.perform(put("/api/orders/500/status")
                .param("status", "SHIPPED") 
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L));

        verify(orderService, times(1)).updateOrderStatus(500L, OrderStatus.SHIPPED);
    }

    /**
     * Verifies the payment simulation endpoint.
     * Ensures boolean parameters are parsed correctly and success flags are propagated.
     */
    @DisplayName("POST /api/orders/{id}/pay - Should return 200 OK and mapped OrderDto")
    @Test
    void testSimulatePayment_Success() throws Exception {
        when(orderService.simulatePayment(500L, true)).thenReturn(mockOrder);
        when(modelMapper.map(any(Order.class), eq(OrderDto.class))).thenReturn(mockOrderDto);

        mockMvc.perform(post("/api/orders/500/pay")
                .param("userId", "1")
                .param("success", "true") 
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L));

        verify(orderService, times(1)).simulatePayment(500L, true);
    }
}