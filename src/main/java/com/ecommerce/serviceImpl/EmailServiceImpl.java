package com.ecommerce.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ecommerce.service.EmailService;

import java.math.BigDecimal;

/**
 * Implementation of {@link EmailService} for managing automated customer communications.
 * This service utilizes Spring's {@link JavaMailSender} to dispatch transactional emails.
 * * It is configured with asynchronous execution to ensure that the user's checkout 
 * experience is not delayed by mail server latency.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    /**
     * Constructs the EmailServiceImpl with a JavaMailSender bean.
     */
    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Dispatches an order confirmation email asynchronously.
     * * @param toEmail The customer's destination email address.
     * @param orderId The unique identifier of the placed order.
     * @param totalAmount The total cost of the transaction.
     * * Note: The {@link Async} annotation requires {@code @EnableAsync} in a 
     * configuration class to function.
     */
    @Async
    @Override
    public void sendOrderConfirmation(String toEmail, Long orderId, BigDecimal totalAmount) {
        logger.info("Attempting to send async order confirmation email to: {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - Order #" + orderId);
            message.setText("Thank you for your purchase!\n\n" +
                            "Your order (ID: " + orderId + ") has been successfully placed.\n" +
                            "Total Amount: ₹" + totalAmount + "\n\n" +
                            "We will notify you once it ships!");

            // Send the constructed message via SMTP
            mailSender.send(message);
            logger.info("Successfully sent order confirmation email for Order ID: {}", orderId);
            
        } catch (Exception e) {
            // Log failure without interrupting the primary application flow
            logger.error("Failed to send order confirmation email for Order ID: {}. Error: {}", orderId, e.getMessage());
        }
    }
}