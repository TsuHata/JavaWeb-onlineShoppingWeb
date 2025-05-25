package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.OrderItemMapper;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.model.dto.MerchantRevenueReportDTO;
import com.example.auth.model.dto.MerchantRevenueReportDTO.RevenueSource;
import com.example.auth.model.dto.MerchantRevenueReportDTO.StatusDistribution;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.OrderItem;
import com.example.auth.service.MerchantReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MerchantReportServiceImpl implements MerchantReportService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public MerchantRevenueReportDTO getMerchantRevenueReport(Long merchantId, String timeRange) {
        MerchantRevenueReportDTO report = new MerchantRevenueReportDTO();
        
        // 获取商家所有订单项
        LambdaQueryWrapper<OrderItem> orderItemQuery = new LambdaQueryWrapper<>();
        orderItemQuery.eq(OrderItem::getMerchantId, merchantId);
        List<OrderItem> allOrderItems = orderItemMapper.selectList(orderItemQuery);
        
        // 获取所有关联的订单ID
        Set<Long> orderIds = allOrderItems.stream()
                .map(OrderItem::getOrderId)
                .collect(Collectors.toSet());
        
        // 获取所有关联订单
        List<Order> orders = new ArrayList<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.in(Order::getId, orderIds);
            orders = orderMapper.selectList(orderQuery);
        }
        
        // 按订单状态分组
        Map<String, List<Order>> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus));
        
        // 计算总收入 (已完成的订单)
        List<Order> completedOrders = ordersByStatus.getOrDefault("completed", Collections.emptyList());
        BigDecimal totalRevenue = calculateTotalRevenue(completedOrders, allOrderItems, merchantId);
        
        // 计算总退款 (退款的订单)
        List<Order> refundedOrders = ordersByStatus.getOrDefault("refunded", Collections.emptyList());
        BigDecimal totalRefunds = calculateTotalRevenue(refundedOrders, allOrderItems, merchantId);
        
        // 设置基础统计数据
        report.setTotalRevenue(totalRevenue);
        report.setTotalOrders(orders.size());
        report.setTotalRefunds(totalRefunds);
        report.setTotalAfterSales(0); // 售后功能已移除
        
        // 生成收入趋势数据
        int days = "week".equals(timeRange) ? 7 : 30;
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d");
        
        List<String> dates = new ArrayList<>();
        List<BigDecimal> revenue = new ArrayList<>();
        List<BigDecimal> refunds = new ArrayList<>();
        
        // 按日期分组订单
        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .filter(order -> order.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        order -> order.getCreateTime().toLocalDate()
                ));
        
        // 按日期分组退款订单
        Map<LocalDate, List<Order>> refundsByDate = refundedOrders.stream()
                .filter(order -> order.getUpdateTime() != null)
                .collect(Collectors.groupingBy(
                        order -> order.getUpdateTime().toLocalDate()
                ));
        
        // 生成每天的收入和退款数据
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(dateFormatter));
            
            // 查找当天的订单并计算收入
            List<Order> dailyOrders = ordersByDate.getOrDefault(date, Collections.emptyList())
                    .stream()
                    .filter(order -> "completed".equals(order.getStatus()))
                    .collect(Collectors.toList());
            BigDecimal dailyRevenue = calculateTotalRevenue(dailyOrders, allOrderItems, merchantId);
            revenue.add(dailyRevenue);
            
            // 查找当天的退款订单并计算退款金额
            List<Order> dailyRefunds = refundsByDate.getOrDefault(date, Collections.emptyList());
            BigDecimal dailyRefund = calculateTotalRevenue(dailyRefunds, allOrderItems, merchantId);
            refunds.add(dailyRefund);
        }
        
        report.setDates(dates);
        report.setRevenue(revenue);
        report.setRefunds(refunds);
        
        // 设置收入构成数据
        List<RevenueSource> revenueSources = new ArrayList<>();
        
        // 假设有两种收入来源：商品销售和增值服务
        // 这里可以根据实际业务来区分不同的收入来源
        BigDecimal productSalesRevenue = totalRevenue.multiply(new BigDecimal("0.9"))
                .setScale(2, RoundingMode.HALF_UP); // 假设商品销售占90%
        BigDecimal valueAddedServicesRevenue = totalRevenue.subtract(productSalesRevenue)
                .setScale(2, RoundingMode.HALF_UP); // 增值服务占10%
        
        RevenueSource productSales = new RevenueSource();
        productSales.setName("商品销售");
        productSales.setValue(productSalesRevenue);
        
        RevenueSource valueAddedServices = new RevenueSource();
        valueAddedServices.setName("增值服务");
        valueAddedServices.setValue(valueAddedServicesRevenue);
        
        revenueSources.add(productSales);
        revenueSources.add(valueAddedServices);
        report.setRevenueSources(revenueSources);
        
        // 设置订单与售后数据
        List<String> months = generateLastSixMonths();
        List<Integer> orderCounts = new ArrayList<>();
        List<Integer> afterSaleCounts = new ArrayList<>();
        
        // 按月份分组订单
        Map<String, List<Order>> ordersByMonth = orders.stream()
                .filter(order -> order.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        order -> order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                ));
        
        // 生成每月的订单数和售后数
        LocalDate currentDate = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = currentDate.minusMonths(i);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            orderCounts.add(ordersByMonth.getOrDefault(monthKey, Collections.emptyList()).size());
            afterSaleCounts.add(0); // 售后功能已移除，所以设置为0
        }
        
        report.setMonths(months);
        report.setOrders(orderCounts);
        report.setAfterSales(afterSaleCounts);
        
        // 设置订单状态分布
        List<StatusDistribution> orderStatus = new ArrayList<>();
        
        // 添加各种订单状态的统计
        addStatusDistribution(orderStatus, "待付款", ordersByStatus.getOrDefault("pending", Collections.emptyList()).size());
        addStatusDistribution(orderStatus, "待发货", ordersByStatus.getOrDefault("paid", Collections.emptyList()).size());
        addStatusDistribution(orderStatus, "待收货", ordersByStatus.getOrDefault("shipped", Collections.emptyList()).size());
        addStatusDistribution(orderStatus, "已完成", ordersByStatus.getOrDefault("completed", Collections.emptyList()).size());
        addStatusDistribution(orderStatus, "已取消", ordersByStatus.getOrDefault("cancelled", Collections.emptyList()).size());
        
        report.setOrderStatus(orderStatus);
        
        return report;
    }

    @Override
    public Map<String, Object> getOrderStatusDistribution(Long merchantId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取商家所有订单项
        LambdaQueryWrapper<OrderItem> orderItemQuery = new LambdaQueryWrapper<>();
        orderItemQuery.eq(OrderItem::getMerchantId, merchantId);
        List<OrderItem> allOrderItems = orderItemMapper.selectList(orderItemQuery);
        
        // 获取所有关联的订单ID
        Set<Long> orderIds = allOrderItems.stream()
                .map(OrderItem::getOrderId)
                .collect(Collectors.toSet());
        
        // 获取所有关联订单
        List<Order> orders = new ArrayList<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.in(Order::getId, orderIds);
            orders = orderMapper.selectList(orderQuery);
        }
        
        // 按订单状态分组
        Map<String, List<Order>> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus));
        
        List<Map<String, Object>> data = new ArrayList<>();
        
        // 添加各种订单状态的统计
        addStatusData(data, "待付款", ordersByStatus.getOrDefault("pending", Collections.emptyList()).size());
        addStatusData(data, "待发货", ordersByStatus.getOrDefault("paid", Collections.emptyList()).size());
        addStatusData(data, "待收货", ordersByStatus.getOrDefault("shipped", Collections.emptyList()).size());
        addStatusData(data, "已完成", ordersByStatus.getOrDefault("completed", Collections.emptyList()).size());
        addStatusData(data, "已取消", ordersByStatus.getOrDefault("cancelled", Collections.emptyList()).size());
        
        result.put("data", data);
        
        return result;
    }
    
    // 生成过去六个月的月份名称列表
    private List<String> generateLastSixMonths() {
        List<String> months = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月");
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = currentDate.minusMonths(i);
            months.add(monthDate.format(formatter));
        }
        
        return months;
    }
    
    // 计算指定订单列表中属于该商家的总收入
    private BigDecimal calculateTotalRevenue(List<Order> orders, List<OrderItem> allOrderItems, Long merchantId) {
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 获取所有订单ID
        Set<Long> orderIds = orders.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());
        
        // 过滤出属于这些订单且属于该商家的订单项
        BigDecimal totalRevenue = allOrderItems.stream()
                .filter(item -> orderIds.contains(item.getOrderId()) && item.getMerchantId().equals(merchantId))
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalRevenue;
    }
    
    // 添加订单状态分布
    private void addStatusDistribution(List<StatusDistribution> statusList, String statusName, int count) {
        StatusDistribution status = new StatusDistribution();
        status.setName(statusName);
        status.setValue(count);
        statusList.add(status);
    }
    
    // 添加订单状态数据（用于Map）
    private void addStatusData(List<Map<String, Object>> dataList, String statusName, int count) {
        Map<String, Object> status = new HashMap<>();
        status.put("name", statusName);
        status.put("value", count);
        dataList.add(status);
    }
} 