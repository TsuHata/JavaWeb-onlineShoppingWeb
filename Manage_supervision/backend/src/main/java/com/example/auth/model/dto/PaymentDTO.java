package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private String paymentNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String transactionId;
    private LocalDateTime refundTime;
    private BigDecimal refundAmount;
    private String refundReason;

}