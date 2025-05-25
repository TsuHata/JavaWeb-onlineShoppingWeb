package com.example.auth.controller;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ProductDTO;
import com.example.auth.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取已审核通过的商品列表
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductDTO>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ProductDTO> response = productService.getAllProducts(page, size, "approved");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        
        // 只返回已审核通过的商品
        if (product == null || !"approved".equals(product.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(product);
    }

    /**
     * 根据分类获取商品
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PageResponse<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ProductDTO> response = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ProductDTO> response = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(response);
    }
} 