package com.example.auth.service.impl;

import com.example.auth.common.exception.BusinessException;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.mapper.PaymentMapper;
import com.example.auth.model.dto.PaymentDTO;
import com.example.auth.model.dto.PaymentRequest;
import com.example.auth.model.dto.RefundRequest;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.Payment;
import com.example.auth.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public PaymentDTO createPayment(Long userId, PaymentRequest request) {
        // 参数校验
        if (request.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().isEmpty()) {
            throw new BusinessException("支付方式不能为空");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("支付金额必须大于0");
        }

        // 查询订单
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }

        // 校验支付金额
        if (request.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new BusinessException("支付金额与订单金额不一致");
        }

        // 创建支付记录
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        // 生成支付单号
        String paymentNumber = generatePaymentNumber();
        payment.setPaymentNumber(paymentNumber);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("pending"); // 待支付
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        paymentMapper.insert(payment);

        return convertToDTO(payment, order.getOrderNumber());
    }

    @Override
    @Transactional
    public PaymentDTO completePayment(Long paymentId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        if (!"pending".equals(payment.getStatus())) {
            throw new BusinessException("支付状态不允许完成");
        }

        // 修改支付状态
        payment.setStatus("completed");
        payment.setUpdateTime(LocalDateTime.now());
        payment.setTransactionId(generateTransactionId());
        paymentMapper.updateById(payment);

        // 修改订单状态
        Order order = orderMapper.selectById(payment.getOrderId());
        if (order != null) {
            order.setStatus("paid");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        return convertToDTO(payment, order != null ? order.getOrderNumber() : null);
    }

    @Override
    @Transactional
    public boolean cancelPayment(Long paymentId, Long userId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        Order order = orderMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此支付");
        }

        if (!"pending".equals(payment.getStatus())) {
            throw new BusinessException("支付状态不允许取消");
        }

        // 修改支付状态
        payment.setStatus("cancelled");
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        return true;
    }

    @Override
    @Transactional
    public PaymentDTO refund(RefundRequest request) {
        if (request.getPaymentId() == null) {
            throw new BusinessException("支付ID不能为空");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额必须大于0");
        }

        Payment payment = paymentMapper.selectById(request.getPaymentId());
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        if (!"completed".equals(payment.getStatus())) {
            throw new BusinessException("支付状态不允许退款");
        }

        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new BusinessException("退款金额不能大于支付金额");
        }

        // 修改支付状态
        payment.setStatus("refunded");
        payment.setUpdateTime(LocalDateTime.now());
        payment.setRefundTime(LocalDateTime.now());
        payment.setRefundAmount(request.getAmount());
        payment.setRefundReason(request.getReason());
        paymentMapper.updateById(payment);

        // 修改订单状态
        Order order = orderMapper.selectById(payment.getOrderId());
        if (order != null) {
            order.setStatus("refunded");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        return convertToDTO(payment, order != null ? order.getOrderNumber() : null);
    }

    @Override
    public List<PaymentDTO> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentMapper.findByOrderId(orderId);
        Order order = orderMapper.selectById(orderId);
        String orderNumber = order != null ? order.getOrderNumber() : null;
        
        return payments.stream()
                .map(payment -> convertToDTO(payment, orderNumber))
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByUserId(Long userId) {
        List<Payment> payments = paymentMapper.findByUserId(userId);
        
        return payments.stream()
                .map(payment -> {
                    Order order = orderMapper.selectById(payment.getOrderId());
                    String orderNumber = order != null ? order.getOrderNumber() : null;
                    return convertToDTO(payment, orderNumber);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO getPaymentDetail(Long paymentId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        
        Order order = orderMapper.selectById(payment.getOrderId());
        String orderNumber = order != null ? order.getOrderNumber() : null;
        
        return convertToDTO(payment, orderNumber);
    }

    /**
     * 生成支付单号
     */
    private String generatePaymentNumber() {
        // 时间戳 + 6位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String random = UUID.randomUUID().toString().substring(0, 6);
        return "PAY" + timestamp + random;
    }

    /**
     * 生成交易号
     */
    private String generateTransactionId() {
        // 时间戳 + 10位随机数
        String random = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        return "TXN" + System.currentTimeMillis() + random;
    }

    /**
     * 转换为DTO
     */
    private PaymentDTO convertToDTO(Payment payment, String orderNumber) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setOrderNumber(orderNumber);
        dto.setPaymentNumber(payment.getPaymentNumber());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setCreateTime(payment.getCreateTime());
        dto.setUpdateTime(payment.getUpdateTime());
        dto.setTransactionId(payment.getTransactionId());
        dto.setRefundTime(payment.getRefundTime());
        dto.setRefundAmount(payment.getRefundAmount());
        dto.setRefundReason(payment.getRefundReason());
        
        return dto;
    }
} 