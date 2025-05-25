package com.example.auth.model.dto;

import java.util.List;

public class UserAssignmentDTO {
    private Long merchantId;
    private List<Long> userIds;
    
    // Getters
    public Long getMerchantId() {
        return merchantId;
    }
    
    public List<Long> getUserIds() {
        return userIds;
    }
    
    // Setters
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
} 