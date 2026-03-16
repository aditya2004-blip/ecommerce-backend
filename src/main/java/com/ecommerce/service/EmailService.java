package com.ecommerce.service;

import java.math.BigDecimal;

/**
 * Service interface for handling automated email communications.
 * Provides the contract for sending transactional emails to users 
 * following critical business events, such as order placement.
 */
public interface EmailService {

    /**
     * Dispatches an order confirmation email to the customer.
     * This method is typically invoked asynchronously after a successful 
     * checkout process to provide the user with a receipt of their purchase.
     * * @param toEmail The recipient's email address.
     * @param orderId The unique identifier of the confirmed order.
     * @param totalAmount The total monetary value of the transaction.
     */
    void sendOrderConfirmation(String toEmail, Long orderId, BigDecimal totalAmount);
}