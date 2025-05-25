package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.common.exception.BusinessException;
import com.example.auth.mapper.OrderItemMapper;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.mapper.ProductMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.CreateOrderRequest;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.dto.OrderItemDTO;
import com.example.auth.model.dto.PaymentDTO;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.OrderItem;
import com.example.auth.model.entity.Product;
import com.example.auth.model.entity.User;
import com.example.auth.service.OrderService;
import com.example.auth.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PaymentService paymentService;

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // 参数校验
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("订单至少需要一个商品");
        }

        if (request.getAddress() == null || request.getAddress().isEmpty()) {
            throw new BusinessException("收货地址不能为空");
        }

        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            throw new BusinessException("联系电话不能为空");
        }

        if (request.getRecipientName() == null || request.getRecipientName().isEmpty()) {
            throw new BusinessException("收货人不能为空");
        }

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        // 生成订单号：时间戳 + 随机字符串
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);
        order.setStatus("pending"); // 待支付
        order.setAddress(request.getAddress());
        order.setPhone(request.getPhone());
        order.setRecipientName(request.getRecipientName());
        order.setRemark(request.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 计算总金额并检查库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMapper.selectById(itemRequest.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在，ID: " + itemRequest.getProductId());
            }

            if (!"approved".equals(product.getStatus())) {
                throw new BusinessException("商品未审核通过，不能购买");
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException("商品库存不足，商品：" + product.getName());
            }

            // 减少商品库存
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productMapper.updateById(product);

            // 创建订单商品
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSubtotal(product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity())));
            orderItem.setMerchantId(product.getMerchantId());

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        // 保存订单
        orderMapper.insert(order);

        // 保存订单商品
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }

        return convertToDTO(order, orderItems);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        if (!"pending".equals(order.getStatus()) && !"paid".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许取消");
        }

        // 恢复商品库存
        List<OrderItem> orderItems = orderItemMapper.findByOrderId(orderId);
        for (OrderItem orderItem : orderItems) {
            Product product = productMapper.selectById(orderItem.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                productMapper.updateById(product);
            }
        }

        order.setStatus("cancelled");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return true;
    }

    @Override
    public Page<OrderDTO> getUserOrders(Long userId, int page, int size) {
        Page<Order> orderPage = new Page<>(page, size);
        orderPage = orderMapper.findByUserId(orderPage, userId);

        return convertToDTO(orderPage);
    }

    @Override
    public Page<OrderDTO> getMerchantOrders(Long merchantId, int page, int size) {
        Page<Order> orderPage = new Page<>(page, size);
        orderPage = orderMapper.findByMerchantId(orderPage, merchantId);

        return convertToDTO(orderPage);
    }

    @Override
    public OrderDTO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        List<OrderItem> orderItems = orderItemMapper.findByOrderId(orderId);
        List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(orderId);

        return convertToDTO(order, orderItems, payments.size() > 0 ? payments.get(0) : null);
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, String status, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 根据不同状态做对应的处理
        switch (status) {
            case "paid":
                if (!"pending".equals(order.getStatus())) {
                    throw new BusinessException("只有待支付的订单才能标记为已支付");
                }
                break;
            case "shipped":
                if (!"paid".equals(order.getStatus())) {
                    throw new BusinessException("只有已支付的订单才能标记为已发货");
                }
                break;
            case "completed":
                if (!"shipped".equals(order.getStatus())) {
                    throw new BusinessException("只有已发货的订单才能标记为已完成");
                }
                break;
            case "cancelled":
                if (!Arrays.asList("pending", "paid").contains(order.getStatus())) {
                    throw new BusinessException("只有待支付或已支付的订单才能取消");
                }
                // 取消订单，恢复库存
                handleCancelOrder(order);
                break;
            case "refunded":
                if (!Arrays.asList("paid", "shipped").contains(order.getStatus())) {
                    throw new BusinessException("只有已支付或已发货的订单才能退款");
                }
                // 退款，恢复库存
                handleCancelOrder(order);
                break;
            default:
                throw new BusinessException("不支持的订单状态：" + status);
        }

        // 更新订单状态
        order.setStatus(status);
        
        // 如果有原因，记录在备注字段
        if (reason != null && !reason.isEmpty()) {
            String currentRemark = order.getRemark();
            String statusAction = getStatusActionText(status);
            String newRemark = (currentRemark == null || currentRemark.isEmpty()) 
                ? statusAction + "原因: " + reason 
                : currentRemark + "\n" + statusAction + "原因: " + reason;
            order.setRemark(newRemark);
        }
        
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, String status) {
        // 调用带原因参数的重载方法，原因参数传null
        return updateOrderStatus(orderId, status, null);
    }

    /**
     * 获取状态操作的描述文本
     */
    private String getStatusActionText(String status) {
        switch (status) {
            case "paid":
                return "标记为已支付";
            case "shipped":
                return "标记为已发货";
            case "completed":
                return "标记为已完成";
            case "cancelled":
                return "取消订单";
            case "refunded":
                return "订单退款";
            default:
                return "状态更新";
        }
    }

    @Override
    @Transactional
    public boolean confirmReceived(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }

        if (!"shipped".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许确认收货");
        }

        order.setStatus("completed");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return true;
    }

    @Override
    @Transactional
    public boolean ship(Long orderId, Long merchantId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单中是否有此商家的商品
        List<OrderItem> orderItems = orderItemMapper.findByOrderIdAndMerchantId(orderId, merchantId);
        if (orderItems.isEmpty()) {
            throw new BusinessException("无权操作此订单");
        }

        if (!"paid".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许发货");
        }

        order.setStatus("shipped");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return true;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        // 时间戳 + 6位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String random = UUID.randomUUID().toString().substring(0, 6);
        return timestamp + random;
    }

    /**
     * 转换为DTO
     */
    private OrderDTO convertToDTO(Order order, List<OrderItem> orderItems) {
        return convertToDTO(order, orderItems, null);
    }

    /**
     * 转换为DTO（带支付信息）
     */
    private OrderDTO convertToDTO(Order order, List<OrderItem> orderItems, PaymentDTO payment) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        
        // 获取用户名
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
        }
        
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setAfterSaleStatus(order.getAfterSaleStatus());
        dto.setAfterSaleReason(order.getAfterSaleReason());
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());
        dto.setAddress(order.getAddress());
        dto.setPhone(order.getPhone());
        dto.setRecipientName(order.getRecipientName());
        dto.setRemark(order.getRemark());
        
        // 设置订单商品
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemDTO> itemDTOs = orderItems.stream().map(this::convertToDTO).collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        // 设置支付信息
        dto.setPayment(payment);
        
        return dto;
    }

    /**
     * 转换为DTO（商品）
     */
    private OrderItemDTO convertToDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrderId());
        dto.setProductId(orderItem.getProductId());
        dto.setProductName(orderItem.getProductName());
        dto.setProductPrice(orderItem.getProductPrice());
        dto.setQuantity(orderItem.getQuantity());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setMerchantId(orderItem.getMerchantId());
        
        // 获取商家名称
        User merchant = userMapper.selectById(orderItem.getMerchantId());
        if (merchant != null) {
            dto.setMerchantName(merchant.getUsername());
        }
        
        // 获取商品图片
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product != null) {
            dto.setProductImageUrl(product.getImageUrl());
        }
        
        return dto;
    }

    /**
     * 分页转换为DTO
     */
    private Page<OrderDTO> convertToDTO(Page<Order> orderPage) {
        Page<OrderDTO> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderDTO> records = orderPage.getRecords().stream().map(order -> {
            List<OrderItem> orderItems = orderItemMapper.findByOrderId(order.getId());
            List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(order.getId());
            return convertToDTO(order, orderItems, payments.size() > 0 ? payments.get(0) : null);
        }).collect(Collectors.toList());
        dtoPage.setRecords(records);
        return dtoPage;
    }

    /**
     * 处理取消订单的共用逻辑，恢复库存
     */
    private void handleCancelOrder(Order order) {
        // 获取订单的所有商品
        List<OrderItem> orderItems = orderItemMapper.findByOrderId(order.getId());
        
        // 对每个商品恢复库存
        for (OrderItem orderItem : orderItems) {
            Product product = productMapper.selectById(orderItem.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    @Override
    public Page<OrderDTO> getAllOrders(int page, int size, String orderNumber, String status, Long userId) {
        Page<Order> orderPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加订单号查询条件
        if (StringUtils.hasText(orderNumber)) {
            queryWrapper.like(Order::getOrderNumber, orderNumber);
        }
        
        // 添加状态查询条件
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Order::getStatus, status);
        }
        
        // 添加用户ID查询条件
        if (userId != null) {
            queryWrapper.eq(Order::getUserId, userId);
        }
        
        // 按创建时间倒序排序，最新订单在前
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        // 查询订单
        orderPage = orderMapper.selectPage(orderPage, queryWrapper);
        
        // 转换为DTO
        return convertToDTO(orderPage);
    }
    
    @Override
    @Transactional
    public boolean applyAfterSale(Long orderId, Long userId, String reason) {
        // 获取订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证是否是用户自己的订单
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 检查订单是否已经申请过售后
        if (order.getAfterSaleStatus() != null && !order.getAfterSaleStatus().isEmpty()) {
            throw new BusinessException("该订单已申请过售后，不能重复申请");
        }
        
        // 设置订单为售后中状态
        order.setAfterSaleStatus("pending");  // 售后待处理
        order.setAfterSaleReason(reason);
        order.setStatus("after_sale");  // 更新订单状态为售后中
        order.setUpdateTime(LocalDateTime.now());
        
        // 更新订单
        orderMapper.updateById(order);
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean processAfterSale(Long orderId, Long merchantId, boolean approved, String reason) {
        // 获取订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证订单是否在售后中
        if (!"pending".equals(order.getAfterSaleStatus())) {
            throw new BusinessException("该订单不在售后处理中");
        }
        
        // 检查订单是否属于该商家
        List<OrderItem> orderItems = orderItemMapper.findByOrderIdAndMerchantId(orderId, merchantId);
        if (orderItems.isEmpty()) {
            throw new BusinessException("无权操作此订单");
        }
        
        if (approved) {
            // 同意退款，更新订单状态
            order.setAfterSaleStatus("approved");
            order.setStatus("refunded");  // 订单状态改为已退款
            
            // 恢复商品库存
            handleCancelOrder(order);
            
            // 记录商家处理意见
            String currentRemark = order.getRemark();
            String newRemark = (currentRemark == null || currentRemark.isEmpty())
                ? "售后处理：同意退款，原因：" + reason
                : currentRemark + "\n售后处理：同意退款，原因：" + reason;
            order.setRemark(newRemark);
        } else {
            // 拒绝退款，只更新售后状态
            order.setAfterSaleStatus("rejected");
            order.setStatus("after_sale_rejected");  // 订单状态改为售后拒绝
            
            // 记录商家处理意见
            String currentRemark = order.getRemark();
            String newRemark = (currentRemark == null || currentRemark.isEmpty())
                ? "售后处理：拒绝退款，原因：" + reason
                : currentRemark + "\n售后处理：拒绝退款，原因：" + reason;
            order.setRemark(newRemark);
        }
        
        order.setUpdateTime(LocalDateTime.now());
        
        // 更新订单
        orderMapper.updateById(order);
        
        return true;
    }
    
    @Override
    public Page<OrderDTO> getMerchantAfterSaleOrders(Long merchantId, int page, int size) {
        Page<Order> orderPage = new Page<>(page, size);
        
        // 自定义SQL查询，获取该商家的所有售后订单
        String sql = "SELECT o.* FROM orders o " +
                "INNER JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE oi.merchant_id = #{merchantId} " +
                "AND o.after_sale_status IS NOT NULL " +
                "GROUP BY o.id " +
                "ORDER BY o.update_time DESC";
                
        // 继续使用现有查询，并通过内存过滤售后订单
        Page<Order> allOrdersPage = orderMapper.findByMerchantId(orderPage, merchantId);
        
        // 过滤出有售后状态的订单
        List<Order> afterSaleOrders = allOrdersPage.getRecords().stream()
                .filter(order -> order.getAfterSaleStatus() != null && !order.getAfterSaleStatus().isEmpty())
                .collect(Collectors.toList());
        
        // 创建新的分页对象
        Page<Order> filteredPage = new Page<>(page, size, afterSaleOrders.size());
        filteredPage.setRecords(afterSaleOrders);
        
        return convertToDTO(filteredPage);
    }
} 