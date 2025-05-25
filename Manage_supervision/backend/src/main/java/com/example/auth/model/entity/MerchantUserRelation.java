package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("merchant_user_relations")
public class MerchantUserRelation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("user_id")
    private Long userId;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("status")
    private String status;

    @TableField(exist = false)
    private User merchant;

    @TableField(exist = false)
    private User user;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public Long getUserId() {
        return userId;
    }

    public User getMerchant() {
        return merchant;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getAssignTime() {
        return assignTime;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setMerchant(User merchant) {
        this.merchant = merchant;
        if (merchant != null) {
            this.merchantId = merchant.getId();
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 