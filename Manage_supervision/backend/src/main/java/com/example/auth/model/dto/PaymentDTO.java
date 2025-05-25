package com.example.auth.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    // Getters
    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
} 