package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.common.exception.BusinessException;
import com.example.auth.common.util.TokenUtil;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 获取商家订单列表
     */
    @GetMapping
    public ResponseEntity<?> getMerchantOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long merchantId = tokenUtil.getUserId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            Page<OrderDTO> orders = orderService.getMerchantOrders(merchantId, page, size);
            return ResponseEntity.ok(Map.of("success", true, "data", orders));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取订单列表失败"));
        }
    }

    /**
     * 获取商家售后订单列表
     */
    @GetMapping("/after-sale")
    public ResponseEntity<?> getMerchantAfterSaleOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long merchantId = tokenUtil.getUserId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            Page<OrderDTO> orders = orderService.getMerchantAfterSaleOrders(merchantId, page, size);
            return ResponseEntity.ok(Map.of("success", true, "data", orders));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取售后订单列表失败"));
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        Long merchantId = tokenUtil.getUserId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            OrderDTO order = orderService.getOrderDetail(id);
            // 验证订单是否包含此商家的商品
            boolean hasMerchantItem = order.getItems().stream()
                    .anyMatch(item -> merchantId.equals(item.getMerchantId()));
            if (!hasMerchantItem) {
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
     * 商家发货
     */
    @PostMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        Long merchantId = tokenUtil.getUserId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            boolean success = orderService.ship(id, merchantId);
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "发货失败"));
        }
    }
    
    /**
     * 处理售后申请
     */
    @PostMapping("/{id}/process-after-sale")
    public ResponseEntity<?> processAfterSale(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam String reason) {
        Long merchantId = tokenUtil.getUserId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            boolean success = orderService.processAfterSale(id, merchantId, approved, reason);
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "处理售后申请失败"));
        }
    }
} 