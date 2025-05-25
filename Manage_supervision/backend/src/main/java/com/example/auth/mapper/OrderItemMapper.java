package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    @Select("SELECT * FROM order_items WHERE merchant_id = #{merchantId}")
    List<OrderItem> findByMerchantId(@Param("merchantId") Long merchantId);
    
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId} AND merchant_id = #{merchantId}")
    List<OrderItem> findByOrderIdAndMerchantId(@Param("orderId") Long orderId, @Param("merchantId") Long merchantId);
    
    @Select("SELECT DISTINCT order_id FROM order_items WHERE merchant_id = #{merchantId}")
    List<Long> findOrderIdsByMerchantId(@Param("merchantId") Long merchantId);
} 