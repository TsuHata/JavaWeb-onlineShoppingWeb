package com.example.auth.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    // Getters
    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getAfterSaleStatus() {
        return afterSaleStatus;
    }

    public String getAfterSaleReason() {
        return afterSaleReason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRemark() {
        return remark;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public PaymentDTO getPayment() {
        return payment;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAfterSaleStatus(String afterSaleStatus) {
        this.afterSaleStatus = afterSaleStatus;
    }

    public void setAfterSaleReason(String afterSaleReason) {
        this.afterSaleReason = afterSaleReason;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public void setPayment(PaymentDTO payment) {
        this.payment = payment;
    }
} 