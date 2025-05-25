package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.common.exception.BusinessException;
import com.example.auth.common.util.TokenUtil;
import com.example.auth.model.dto.CreateOrderRequest;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            OrderDTO order = orderService.createOrder(userId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", order));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "创建订单失败"));
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping
    public ResponseEntity<?> getUserOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            Page<OrderDTO> orders = orderService.getUserOrders(userId, page, size);
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
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            OrderDTO order = orderService.getOrderDetail(id);
            // 检查是否是当前用户的订单
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无权访问此订单"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", order));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取订单详情失败"));
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            boolean success = orderService.cancelOrder(id, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "取消订单失败"));
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmReceived(@PathVariable Long id) {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            boolean success = orderService.confirmReceived(id, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "确认收货失败"));
        }
    }
    
    /**
     * 申请售后
     */
    @PostMapping("/{id}/after-sale")
    public ResponseEntity<?> applyAfterSale(
            @PathVariable Long id,
            @RequestParam String reason) {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            boolean success = orderService.applyAfterSale(id, userId, reason);
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "申请售后失败"));
        }
    }

} 