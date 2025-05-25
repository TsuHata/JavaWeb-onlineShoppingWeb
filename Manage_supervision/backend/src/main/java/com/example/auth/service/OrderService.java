package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.CreateOrderRequest;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.entity.Order;

import java.util.List;

public interface OrderService {

    /**
     * 创建订单
     * @param userId 用户ID
     * @param request 创建订单请求
     * @return 订单DTO
     */
    OrderDTO createOrder(Long userId, CreateOrderRequest request);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, Long userId);

    /**
     * 获取用户订单列表（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 订单DTO分页
     */
    Page<OrderDTO> getUserOrders(Long userId, int page, int size);

    /**
     * 获取商家订单列表（分页）
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 订单DTO分页
     */
    Page<OrderDTO> getMerchantOrders(Long merchantId, int page, int size);
    
    /**
     * 获取所有订单列表（分页，管理员使用）
     * @param page 页码
     * @param size 每页大小
     * @param orderNumber 订单号（可选）
     * @param status 订单状态（可选）
     * @param userId 用户ID（可选）
     * @return 订单DTO分页
     */
    Page<OrderDTO> getAllOrders(int page, int size, String orderNumber, String status, Long userId);

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单DTO
     */
    OrderDTO getOrderDetail(Long orderId);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Long orderId, String status);
    
    /**
     * 更新订单状态（带原因）
     * @param orderId 订单ID
     * @param status 状态
     * @param reason 原因说明
     * @return 是否成功
     */
    boolean updateOrderStatus(Long orderId, String status, String reason);

    /**
     * 确认收货
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean confirmReceived(Long orderId, Long userId);

    /**
     * 商家发货
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @return 是否成功
     */
    boolean ship(Long orderId, Long merchantId);
    
    /**
     * 用户申请售后
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 售后原因
     * @return 是否成功
     */
    boolean applyAfterSale(Long orderId, Long userId, String reason);
    
    /**
     * 商家处理售后申请
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @param approved 是否同意
     * @param reason 处理原因
     * @return 是否成功
     */
    boolean processAfterSale(Long orderId, Long merchantId, boolean approved, String reason);
    
    /**
     * 获取商家售后订单列表
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 订单DTO分页
     */
    Page<OrderDTO> getMerchantAfterSaleOrders(Long merchantId, int page, int size);
} 