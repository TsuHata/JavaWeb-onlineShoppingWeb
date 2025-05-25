package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("products")
public class Product {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    @TableField("image_url")
    private String imageUrl;

    private BigDecimal price;

    private Integer stock;

    @TableField("category_id")
    private Long categoryId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // 状态：pending-待审核，approved-已审核，rejected-已拒绝
    private String status;

    @TableField("audit_comment")
    private String auditComment;
    
    @TableField("audit_time")
    private LocalDateTime auditTime;
    
    @TableField("audit_user_id")
    private Long auditUserId;

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getAuditComment() {
        return auditComment;
    }
    
    public LocalDateTime getAuditTime() {
        return auditTime;
    }
    
    public Long getAuditUserId() {
        return auditUserId;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }
    
    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }
    
    public void setAuditUserId(Long auditUserId) {
        this.auditUserId = auditUserId;
    }
} 