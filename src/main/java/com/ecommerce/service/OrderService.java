package com.ecommerce.service;

import java.util.List;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;

/**
 * Service interface defining the business logic for Order management.
 * This service coordinates the transition from a shopping cart to a finalized purchase,
 * manages order history retrieval, and handles lifecycle updates such as status 
 * changes and payment simulations.
 */
public interface OrderService {

    /**
     * Converts a user's current shopping cart into a formal Order.
     * This process includes verifying inventory, calculating the final total, 
     * creating order line items, and clearing the active cart.
     * * @param userId The unique identifier of the user performing the checkout.
     * @return The newly created Order entity.
     * @throws com.ecommerce.exception.ResourceNotFoundException if the cart is empty or user doesn't exist.
     */
	Order checkout(Long userId);

    /**
     * Retrieves all past and present orders associated with a specific user.
     * * @param userId The unique identifier of the customer.
     * @return A list of Order entities placed by the user.
     */
    List<Order> getUserOrders(Long userId);

    /**
     * Fetches the full details of a specific order by its unique ID.
     * * @param id The unique identifier of the order.
     * @return The requested Order entity.
     * @throws com.ecommerce.exception.ResourceNotFoundException if the order ID is invalid.
     */
    Order getOrderById(Long id);

    /**
     * Updates the current status of an order (e.g., from PENDING to SHIPPED).
     * Typically used by administrative roles to manage the fulfillment lifecycle.
     * * @param orderId The unique identifier of the order to be updated.
     * @param status The new {@link OrderStatus} to be applied.
     * @return The updated Order entity.
     */
    Order updateOrderStatus(Long orderId, OrderStatus status);

    /**
     * Simulates the interaction with a third-party payment gateway.
     * Updates the order's payment status and order status based on the success 
     * of the transaction.
     * * @param orderId The unique identifier of the order being paid for.
     * @param success A flag indicating if the payment simulation should succeed or fail.
     * @return The Order entity with updated payment/status information.
     */
    Order simulatePayment(Long orderId, boolean success);
    
}