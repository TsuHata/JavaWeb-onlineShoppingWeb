package com.example.auth.service;

import com.example.auth.model.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    
    /**
     * 获取所有分类（树形结构）
     */
    List<CategoryDTO> getAllCategoriesTree();
    
    /**
     * 获取所有分类（平铺结构）
     */
    List<CategoryDTO> getAllCategories();
    
    /**
     * 创建分类
     */
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    
    /**
     * 更新分类
     */
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
    
    /**
     * 获取分类详情
     */
    CategoryDTO getCategoryById(Long id);
} 