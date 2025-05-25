package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> findByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    Page<Order> findByUserId(Page<?> page, @Param("userId") Long userId);
    
    @Select("SELECT o.* FROM orders o " +
            "INNER JOIN order_items oi ON o.id = oi.order_id " +
            "WHERE oi.merchant_id = #{merchantId} " +
            "GROUP BY o.id " +
            "ORDER BY o.create_time DESC")
    List<Order> findByMerchantId(@Param("merchantId") Long merchantId);
    
    @Select("SELECT o.* FROM orders o " +
            "INNER JOIN order_items oi ON o.id = oi.order_id " +
            "WHERE oi.merchant_id = #{merchantId} " +
            "GROUP BY o.id " +
            "ORDER BY o.create_time DESC")
    Page<Order> findByMerchantId(Page<?> page, @Param("merchantId") Long merchantId);
    
    @Select("SELECT * FROM orders WHERE order_number = #{orderNumber}")
    Order findByOrderNumber(@Param("orderNumber") String orderNumber);
} 