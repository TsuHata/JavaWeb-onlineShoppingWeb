package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.CategoryDTO;
import com.example.auth.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类（树形结构）
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategoriesTree();
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取所有分类（平铺结构）
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesFlat() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 创建分类
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(createdCategory);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        if (updatedCategory != null) {
            return ResponseEntity.ok(updatedCategory);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        if (category != null) {
            return ResponseEntity.ok(category);
        }
        return ResponseEntity.notFound().build();
    }
} 