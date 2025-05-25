package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
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

}