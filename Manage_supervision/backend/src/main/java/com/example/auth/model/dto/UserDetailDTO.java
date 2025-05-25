package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDetailDTO {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String userNumber;
    private String email;
    private String phone;
    private String status;
    private String createTime;
    private String lastLoginTime;
    private List<String> roles;
    private String bio;
    private String avatar;
    private int orderCount;
    private double totalSpent;
    private List<ActivityDTO> recentActivities;
    
    @Data
    public static class ActivityDTO {
        private String type;
        private String title;
        private String content;
        private String time;
    }
} 