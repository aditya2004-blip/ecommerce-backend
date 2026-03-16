package com.ecommerce.entity;

/**
 * Enumeration representing the lifecycle stages of an Order.
 * * This enum is used to track and manage the fulfillment process, from initial 
 * placement to final delivery or cancellation.
 */

public enum OrderStatus {
	/** Order has been successfully created and is awaiting processing. */
    PLACED, 
    
    /** Order has been packed and handed over to the logistics provider. */
    SHIPPED, 
    
    /** Order has been successfully received by the customer. */
    DELIVERED, 
    
    /** Order has been voided by the user or the administrator. */
    CANCELLED
}
