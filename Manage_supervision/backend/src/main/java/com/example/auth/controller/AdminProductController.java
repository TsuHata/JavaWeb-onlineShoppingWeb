package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ProductAuditDTO;
import com.example.auth.model.dto.ProductDTO;
import com.example.auth.service.ProductService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequireRole("ADMIN")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 管理员获取所有商品列表，支持多种筛选条件
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        
        PageResponse<ProductDTO> response = productService.getProductsWithFilters(
                page, size, status, merchantId, categoryId, 
                minPrice, maxPrice, minStock, maxStock, 
                startDate, endDate, keyword, sortBy, sortDirection);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 审核商品
     */
    @PostMapping("/audit")
    public ResponseEntity<?> auditProduct(@RequestBody ProductAuditDTO auditDTO) {
        try {
            Long adminId = UserContext.getCurrentUserId();
            ProductDTO product = productService.auditProduct(auditDTO, adminId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取待审核商品列表
     */
    @GetMapping("/pending")
    public ResponseEntity<PageResponse<ProductDTO>> getPendingProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<ProductDTO> response = productService.getProductsWithFilters(
                page, size, "pending", merchantId, categoryId, 
                null, null, null, null, 
                null, null, keyword, "id", "desc");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取已审核商品列表
     */
    @GetMapping("/approved")
    public ResponseEntity<PageResponse<ProductDTO>> getApprovedProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<ProductDTO> response = productService.getProductsWithFilters(
                page, size, "approved", merchantId, categoryId, 
                null, null, null, null, 
                null, null, keyword, "id", "desc");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取被拒绝商品列表
     */
    @GetMapping("/rejected")
    public ResponseEntity<PageResponse<ProductDTO>> getRejectedProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<ProductDTO> response = productService.getProductsWithFilters(
                page, size, "rejected", merchantId, categoryId, 
                null, null, null, null, 
                null, null, keyword, "id", "desc");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 管理员更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        try {
            // 使用管理员ID进行操作，但实际保留商品的原始商家ID
            Long adminId = UserContext.getCurrentUserId();
            ProductDTO updatedProduct = productService.adminUpdateProduct(id, productDTO, adminId);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 管理员删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            Long adminId = UserContext.getCurrentUserId();
            productService.adminDeleteProduct(id, adminId);
            return ResponseEntity.ok(Map.of("message", "商品删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 