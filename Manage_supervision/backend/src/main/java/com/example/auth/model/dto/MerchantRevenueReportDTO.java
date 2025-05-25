package com.example.auth.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class MerchantRevenueReportDTO {
    // 统计数据
    private BigDecimal totalRevenue;      // 总收入
    private int totalOrders;              // 总订单数
    private BigDecimal totalRefunds;      // 总退款金额
    private int totalAfterSales;          // 总售后数量
    
    // 收入趋势数据
    private List<String> dates;           // 日期列表
    private List<BigDecimal> revenue;     // 每日收入
    private List<BigDecimal> refunds;     // 每日退款
    
    // 收入构成
    private List<RevenueSource> revenueSources;
    
    // 订单与售后数据
    private List<String> months;          // 月份列表
    private List<Integer> orders;         // 每月订单数
    private List<Integer> afterSales;     // 每月售后数
    
    // 订单状态分布
    private List<StatusDistribution> orderStatus;
    
    @Data
    public static class RevenueSource {
        private String name;
        private BigDecimal value;
    }
    
    @Data
    public static class StatusDistribution {
        private String name;
        private int value;
    }
} 