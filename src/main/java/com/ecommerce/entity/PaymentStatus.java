package com.ecommerce.entity;

/**
 * Enumeration representing the state of a financial transaction for an Order.
 * * This enum tracks the result of payment gateway interactions, determining 
 * whether an order can proceed to fulfillment or requires customer intervention.
 */
public enum PaymentStatus {

    /** Payment has been initialized but not yet confirmed by the gateway. */
	PENDING, 
    
    /** Transaction completed successfully and funds have been authorized/captured. */
    SUCCESS, 
    
    /** Transaction was declined by the bank or aborted by the user. */
    FAILED
}