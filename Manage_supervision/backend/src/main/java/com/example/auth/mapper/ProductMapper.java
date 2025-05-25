package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据商家ID查询产品列表（分页）
     */
    @Select("SELECT p.* FROM products p WHERE p.merchant_id = #{merchantId} ORDER BY p.create_time DESC")
    Page<Product> findByMerchantId(Page<Product> page, @Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID和审核状态查询产品
     */
    @Select("SELECT p.* FROM products p WHERE p.merchant_id = #{merchantId} AND p.status = #{status} ORDER BY p.create_time DESC")
    Page<Product> findByMerchantIdAndStatus(Page<Product> page, @Param("merchantId") Long merchantId, @Param("status") String status);
    
    /**
     * 根据审核状态查询产品
     */
    @Select("SELECT p.* FROM products p WHERE p.status = #{status} ORDER BY p.create_time DESC")
    Page<Product> findByStatus(Page<Product> page, @Param("status") String status);
    
    /**
     * 查询所有产品（分页）
     */
    @Select("SELECT p.* FROM products p ORDER BY p.create_time DESC")
    Page<Product> findAllProducts(Page<Product> page);
    
    /**
     * 根据分类ID查询产品
     */
    @Select("SELECT p.* FROM products p WHERE p.category_id = #{categoryId} AND p.status = 'approved' ORDER BY p.create_time DESC")
    Page<Product> findByCategoryId(Page<Product> page, @Param("categoryId") Long categoryId);
    
    /**
     * 根据名称模糊搜索产品
     */
    @Select("SELECT p.* FROM products p WHERE p.name LIKE CONCAT('%', #{keyword}, '%') AND p.status = 'approved' ORDER BY p.create_time DESC")
    Page<Product> searchProducts(Page<Product> page, @Param("keyword") String keyword);
} 