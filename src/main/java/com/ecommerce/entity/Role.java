package com.ecommerce.entity;

/**
 * Enumeration defining the authorization levels within the system.
 * This enum is utilized by Spring Security to enforce Role-Based Access Control (RBAC),
 * determining which API endpoints a user can interact with.
 */
public enum Role {
    /** * Standard user role. 
     * Permissions include browsing products, managing their own cart, 
     * and placing/viewing their own orders. 
     */
    CUSTOMER,

    /** * Administrative user role. 
     * Permissions include full CRUD operations on products, 
     * updating order statuses, and managing user accounts. 
     */
    ADMIN	
}