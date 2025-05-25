package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("payments")
public class Payment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("payment_number")
    private String paymentNumber;

    private BigDecimal amount;

    @TableField("payment_method")
    private String paymentMethod;

    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("refund_time")
    private LocalDateTime refundTime;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @TableField("refund_reason")
    private String refundReason;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
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