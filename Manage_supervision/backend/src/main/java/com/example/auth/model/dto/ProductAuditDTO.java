package com.example.auth.model.dto;

public class ProductAuditDTO {
    private Long productId;
    private String status; // approved 或 rejected
    private String auditComment;

    // Getters
    public Long getProductId() {
        return productId;
    }

    public String getStatus() {
        return status;
    }

    public String getAuditComment() {
        return auditComment;
    }

    // Setters
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }
} 