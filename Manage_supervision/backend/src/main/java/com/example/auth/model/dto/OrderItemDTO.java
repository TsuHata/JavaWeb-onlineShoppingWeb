package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    // Setters
    // Getters
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Long merchantId;
    private String merchantName;
    private String productImageUrl;

}