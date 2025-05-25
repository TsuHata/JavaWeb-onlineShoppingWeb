package com.example.auth.service;

import com.example.auth.model.dto.PaymentDTO;
import com.example.auth.model.dto.PaymentRequest;
import com.example.auth.model.dto.RefundRequest;

import java.util.List;

public interface PaymentService {

    /**
     * 创建支付
     * @param userId 用户ID
     * @param request 支付请求
     * @return 支付DTO
     */
    PaymentDTO createPayment(Long userId, PaymentRequest request);

    /**
     * 处理支付成功
     * @param paymentId 支付ID
     * @return 支付DTO
     */
    PaymentDTO completePayment(Long paymentId);

    /**
     * 取消支付
     * @param paymentId 支付ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelPayment(Long paymentId, Long userId);

    /**
     * 退款
     * @param request 退款请求
     * @return 支付DTO
     */
    PaymentDTO refund(RefundRequest request);

    /**
     * 获取订单支付记录
     * @param orderId 订单ID
     * @return 支付DTO列表
     */
    List<PaymentDTO> getPaymentsByOrderId(Long orderId);

    /**
     * 获取用户支付记录
     * @param userId 用户ID
     * @return 支付DTO列表
     */
    List<PaymentDTO> getPaymentsByUserId(Long userId);

    /**
     * 获取支付详情
     * @param paymentId 支付ID
     * @return 支付DTO
     */
    PaymentDTO getPaymentDetail(Long paymentId);
} 