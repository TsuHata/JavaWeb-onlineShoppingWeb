package com.example.auth.service;

import com.example.auth.model.dto.MerchantRevenueReportDTO;

import java.util.Map;

public interface MerchantReportService {
    
    /**
     * 获取商家收入报表
     * @param merchantId 商家ID
     * @param timeRange 时间范围：week(近7天), month(近30天)
     * @return 收入报表数据
     */
    MerchantRevenueReportDTO getMerchantRevenueReport(Long merchantId, String timeRange);
    
    /**
     * 获取商家订单状态分布
     * @param merchantId 商家ID
     * @return 订单状态分布数据
     */
    Map<String, Object> getOrderStatusDistribution(Long merchantId);
} 