package com.example.auth.model.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String categoryName; // 分类名称
    private Long merchantId;
    private String merchantName; // 商家名称
    private String createTime;
    private String updateTime;
    private String status; // pending, approved, rejected
    private String auditComment;
    private String auditTime;
    private Long auditUserId;
    private String auditUserName;

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

    public String getCategoryName() {
        return categoryName;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getAuditComment() {
        return auditComment;
    }

    public String getAuditTime() {
        return auditTime;
    }

    public Long getAuditUserId() {
        return auditUserId;
    }

    public String getAuditUserName() {
        return auditUserName;
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

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

    public void setAuditTime(String auditTime) {
        this.auditTime = auditTime;
    }

    public void setAuditUserId(Long auditUserId) {
        this.auditUserId = auditUserId;
    }

    public void setAuditUserName(String auditUserName) {
        this.auditUserName = auditUserName;
    }
} 