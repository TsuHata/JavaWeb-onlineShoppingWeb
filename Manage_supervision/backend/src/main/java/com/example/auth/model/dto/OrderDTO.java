package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {

    private Long id;
    private String orderNumber;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private String afterSaleStatus;
    private String afterSaleReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String address;
    private String phone;
    private String recipientName;
    private String remark;
    private List<OrderItemDTO> items;
    private PaymentDTO payment;

}