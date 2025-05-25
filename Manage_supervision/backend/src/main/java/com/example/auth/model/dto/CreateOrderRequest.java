package com.example.auth.model.dto;

import lombok.Data;


import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
    private String address;
    private String phone;
    private String recipientName;
    private String remark;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;

    }
} 