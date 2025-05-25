package com.example.auth.model.dto;

import lombok.Data;

@Data
public class MerchantUserDTO {
    private Long id;
    private Long merchantId;
    private String merchantName;
    private String merchantUserNumber;
    private Long userId;
    private String userName;
    private String userUserNumber;
    private String assignTime;
    private String status;
} 