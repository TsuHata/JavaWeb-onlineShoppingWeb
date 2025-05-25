package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.MerchantRevenueReportDTO;
import com.example.auth.service.MerchantReportService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/reports")
public class MerchantReportController {

    @Autowired
    private MerchantReportService merchantReportService;
    
    @Autowired
    private UserContext userContext;

    /**
     * 获取商家收入报表数据
     * @param timeRange 时间范围：week(近7天), month(近30天)
     * @return 收入报表数据
     */
    @GetMapping("/revenue")
    @RequireRole("MERCHANT")
    public ResponseEntity<MerchantRevenueReportDTO> getMerchantRevenueReport(
            @RequestParam(value = "timeRange", defaultValue = "week") String timeRange) {
        Long merchantId = userContext.getCurrentUser().getId();
        return ResponseEntity.ok(merchantReportService.getMerchantRevenueReport(merchantId, timeRange));
    }
    
    /**
     * 获取商家订单状态分布
     * @return 订单状态分布数据
     */
    @GetMapping("/order-status")
    @RequireRole("MERCHANT")
    public ResponseEntity<Map<String, Object>> getOrderStatusDistribution() {
        Long merchantId = userContext.getCurrentUser().getId();
        return ResponseEntity.ok(merchantReportService.getOrderStatusDistribution(merchantId));
    }
} 