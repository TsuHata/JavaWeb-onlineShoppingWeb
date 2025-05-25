package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MerchantDashboardDTO {
    private int userCount;
    private int activeToday;
    private int pendingOrders;
    private int weeklyEvents;
    private List<Map<String, Object>> userActivities;
    private List<RecentActivity> recentActivities;
    
    @Data
    public static class RecentActivity {
        private String type;
        private String title;
        private String content;
        private String time;
    }
} 