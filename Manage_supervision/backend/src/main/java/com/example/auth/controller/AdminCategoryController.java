package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.CategoryDTO;
import com.example.auth.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 管理员获取所有分类树
     */
    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategoriesTree();
        return ResponseEntity.ok(categories);
    }

    /**
     * 管理员创建分类
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(createdCategory);
    }

    /**
     * 管理员更新分类
     */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
            if (updatedCategory != null) {
                return ResponseEntity.ok(updatedCategory);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 管理员删除分类
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 管理员获取分类详情
     */
    @GetMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        if (category != null) {
            return ResponseEntity.ok(category);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 批量添加分类
     */
    @PostMapping("/batch")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> batchCreateCategories(@RequestBody List<CategoryDTO> categories) {
        int successCount = 0;
        
        for (CategoryDTO categoryDTO : categories) {
            try {
                categoryService.createCategory(categoryDTO);
                successCount++;
            } catch (Exception e) {
                // 记录失败的情况，但继续处理
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", categories.size());
        result.put("successCount", successCount);
        result.put("failCount", categories.size() - successCount);
        
        return ResponseEntity.ok(result);
    }
} 