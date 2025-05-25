package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
public class Order {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_number")
    private String orderNumber;

    @TableField("user_id")
    private Long userId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    private String status;
    
    @TableField("after_sale_status")
    private String afterSaleStatus;
    
    @TableField("after_sale_reason")
    private String afterSaleReason;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    private String address;

    private String phone;

    @TableField("recipient_name")
    private String recipientName;

    private String remark;

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
} 