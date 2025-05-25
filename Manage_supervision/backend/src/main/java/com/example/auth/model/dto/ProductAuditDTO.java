package com.example.auth.model.dto;

import lombok.Data;

@Data
public class ProductAuditDTO {

    private Long productId;
    private String status; // approved 或 rejected
    private String auditComment;

}