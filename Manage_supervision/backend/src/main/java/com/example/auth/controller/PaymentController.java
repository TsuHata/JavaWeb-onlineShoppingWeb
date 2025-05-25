package com.example.auth.controller;

import com.example.auth.common.exception.BusinessException;
import com.example.auth.common.util.TokenUtil;
import com.example.auth.model.dto.PaymentDTO;
import com.example.auth.model.dto.PaymentRequest;
import com.example.auth.model.dto.RefundRequest;
import com.example.auth.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 创建支付
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            PaymentDTO payment = paymentService.createPayment(userId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", payment));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "创建支付失败"));
        }
    }

    /**
     * 完成支付（模拟支付成功回调）
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completePayment(@PathVariable Long id) {
        try {
            PaymentDTO payment = paymentService.completePayment(id);
            return ResponseEntity.ok(Map.of("success", true, "data", payment));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "完成支付失败"));
        }
    }

    /**
     * 取消支付
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long id) {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            boolean success = paymentService.cancelPayment(id, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", success));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "取消支付失败"));
        }
    }

    /**
     * 退款
     */
    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestBody RefundRequest request) {
        try {
            PaymentDTO payment = paymentService.refund(request);
            return ResponseEntity.ok(Map.of("success", true, "data", payment));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "退款失败"));
        }
    }

    /**
     * 获取用户支付记录
     */
    @GetMapping
    public ResponseEntity<?> getUserPayments() {
        Long userId = tokenUtil.getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请先登录"));
        }

        try {
            List<PaymentDTO> payments = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", payments));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取支付记录失败"));
        }
    }

    /**
     * 获取订单支付记录
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderPayments(@PathVariable Long orderId) {
        try {
            List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(orderId);
            return ResponseEntity.ok(Map.of("success", true, "data", payments));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取支付记录失败"));
        }
    }

    /**
     * 获取支付详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentDetail(@PathVariable Long id) {
        try {
            PaymentDTO payment = paymentService.getPaymentDetail(id);
            return ResponseEntity.ok(Map.of("success", true, "data", payment));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取支付详情失败"));
        }
    }
} 