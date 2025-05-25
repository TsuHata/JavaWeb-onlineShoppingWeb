package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ProductAuditDTO;
import com.example.auth.model.dto.ProductDTO;

import java.math.BigDecimal;

public interface ProductService {
    
    /**
     * 创建商品
     */
    ProductDTO createProduct(ProductDTO productDTO, Long merchantId);
    
    /**
     * 更新商品
     */
    ProductDTO updateProduct(Long id, ProductDTO productDTO, Long merchantId);
    
    /**
     * 获取商品详情
     */
    ProductDTO getProductById(Long id);
    
    /**
     * 商家获取自己的商品列表
     */
    PageResponse<ProductDTO> getMerchantProducts(Long merchantId, int page, int size, String status);
    
    /**
     * 管理员获取所有商品列表
     */
    PageResponse<ProductDTO> getAllProducts(int page, int size, String status);
    
    /**
     * 管理员获取商品列表（支持多种筛选条件）
     */
    PageResponse<ProductDTO> getProductsWithFilters(
            int page, int size, String status, Long merchantId, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minStock, Integer maxStock,
            String startDate, String endDate, String keyword, String sortBy, String sortDirection);
    
    /**
     * 管理员审核商品
     */
    ProductDTO auditProduct(ProductAuditDTO auditDTO, Long adminId);
    
    /**
     * 根据分类获取商品列表
     */
    PageResponse<ProductDTO> getProductsByCategory(Long categoryId, int page, int size);
    
    /**
     * 搜索商品
     */
    PageResponse<ProductDTO> searchProducts(String keyword, int page, int size);
    
    /**
     * 删除商品
     */
    void deleteProduct(Long id, Long merchantId);
    
    /**
     * 上传商品图片
     */
    String uploadProductImage(byte[] imageData, String filename);
    
    /**
     * 管理员更新商品信息（保留原始商家ID）
     */
    ProductDTO adminUpdateProduct(Long id, ProductDTO productDTO, Long adminId);
    
    /**
     * 管理员删除商品（无需校验商家ID）
     */
    void adminDeleteProduct(Long id, Long adminId);
} 