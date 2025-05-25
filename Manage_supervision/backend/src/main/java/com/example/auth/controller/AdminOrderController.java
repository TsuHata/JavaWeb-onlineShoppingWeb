package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.common.exception.BusinessException;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员订单控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取所有订单列表（分页）
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        
        try {
            // 调用服务层获取所有订单
            Page<OrderDTO> orders = orderService.getAllOrders(page, size, orderNumber, status, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", orders));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取订单列表失败"));
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderDetail(id);
            return ResponseEntity.ok(Map.of("success", true, "data", order));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取订单详情失败"));
        }
    }

    /**
     * 更新订单状态
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        
        try {
            boolean success;
            if (reason != null && !reason.isEmpty()) {
                success = orderService.updateOrderStatus(id, status, reason);
            } else {
                success = orderService.updateOrderStatus(id, status);
            }
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "更新订单状态失败"));
        }
    }

    /**
     * 取消订单（管理员操作）
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        try {
            boolean success = orderService.updateOrderStatus(id, "CANCELED", 
                    reason != null ? reason : "管理员取消订单");
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "取消订单失败"));
        }
    }
} 