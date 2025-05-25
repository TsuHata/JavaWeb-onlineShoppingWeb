package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ProductDTO;
import com.example.auth.service.ProductService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/products")
@RequireRole("MERCHANT")
public class MerchantProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商家创建商品
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        Long merchantId = UserContext.getCurrentUserId();
        ProductDTO createdProduct = productService.createProduct(productDTO, merchantId);
        return ResponseEntity.ok(createdProduct);
    }

    /**
     * 商家更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        Long merchantId = UserContext.getCurrentUserId();
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO, merchantId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 商家获取自己的商品列表
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductDTO>> getMerchantProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Long merchantId = UserContext.getCurrentUserId();
        PageResponse<ProductDTO> response = productService.getMerchantProducts(merchantId, page, size, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Long merchantId = UserContext.getCurrentUserId();
        ProductDTO product = productService.getProductById(id);
        
        // 验证商品是否属于当前商家
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(product);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Long merchantId = UserContext.getCurrentUserId();
        try {
            productService.deleteProduct(id, merchantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            byte[] imageData = file.getBytes();
            String filename = file.getOriginalFilename();
            String imageUrl = productService.uploadProductImage(imageData, filename);
            
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 