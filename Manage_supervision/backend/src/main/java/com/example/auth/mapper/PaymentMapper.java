package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
    
    @Select("SELECT * FROM payments WHERE order_id = #{orderId}")
    List<Payment> findByOrderId(@Param("orderId") Long orderId);
    
    @Select("SELECT * FROM payments WHERE payment_number = #{paymentNumber}")
    Payment findByPaymentNumber(@Param("paymentNumber") String paymentNumber);
    
    @Select("SELECT p.* FROM payments p " +
            "INNER JOIN orders o ON p.order_id = o.id " +
            "WHERE o.user_id = #{userId}")
    List<Payment> findByUserId(@Param("userId") Long userId);
} 