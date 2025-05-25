package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.model.dto.CategoryDTO;
import com.example.auth.model.entity.Category;
import com.example.auth.mapper.CategoryMapper;
import com.example.auth.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<CategoryDTO> getAllCategoriesTree() {
        // 获取所有根分类
        List<Category> rootCategories = categoryMapper.findRootCategories();
        
        // 构建分类树
        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryDTO> getAllCategories() {
        // 获取所有分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getParentId).orderByAsc(Category::getId);
        
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        
        return categories.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }
    
    private CategoryDTO buildCategoryTree(Category category) {
        CategoryDTO dto = convertToCategoryDTO(category);
        
        // 查找子分类
        List<Category> children = categoryMapper.findByParentId(category.getId());
        if (children != null && !children.isEmpty()) {
            List<CategoryDTO> childrenDTOs = children.stream()
                    .map(this::buildCategoryTree)
                    .collect(Collectors.toList());
            dto.setChildren(childrenDTOs);
        } else {
            dto.setChildren(new ArrayList<>());
        }
        
        return dto;
    }
    
    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setParentId(categoryDTO.getParentId());
        category.setCreateTime(LocalDateTime.now());
        
        categoryMapper.insert(category);
        
        return convertToCategoryDTO(category);
    }
    
    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryMapper.selectById(id);
        if (category != null) {
            category.setName(categoryDTO.getName());
            
            // 检查是否修改了父级ID，需要防止循环引用
            if (categoryDTO.getParentId() != null && !categoryDTO.getParentId().equals(category.getParentId())) {
                // 不能将分类的父级设置为自己
                if (categoryDTO.getParentId().equals(id)) {
                    throw new IllegalArgumentException("分类不能设置自己为父级");
                }
                
                // 不能将分类的父级设置为自己的子级
                if (isChildCategory(id, categoryDTO.getParentId())) {
                    throw new IllegalArgumentException("不能将分类的父级设置为自己的子级");
                }
                
                category.setParentId(categoryDTO.getParentId());
            }
            
            categoryMapper.updateById(category);
            return convertToCategoryDTO(category);
        }
        return null;
    }
    
    /**
     * 检查targetId是否是categoryId的子分类
     */
    private boolean isChildCategory(Long categoryId, Long targetId) {
        if (targetId == null) {
            return false;
        }
        
        Category targetCategory = categoryMapper.selectById(targetId);
        if (targetCategory == null) {
            return false;
        }
        
        // 如果目标分类的父ID就是当前分类，说明是子分类
        if (categoryId.equals(targetCategory.getParentId())) {
            return true;
        }
        
        // 递归检查上级分类
        return isChildCategory(categoryId, targetCategory.getParentId());
    }
    
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 查找该分类下的所有子分类
        List<Category> children = categoryMapper.findByParentId(id);
        if (children != null && !children.isEmpty()) {
            // 递归删除所有子分类
            for (Category child : children) {
                deleteCategory(child.getId());
            }
        }
        
        // 删除当前分类
        categoryMapper.deleteById(id);
    }
    
    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category != null) {
            return convertToCategoryDTO(category);
        }
        return null;
    }
    
    private CategoryDTO convertToCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setParentId(category.getParentId());
        
        // 格式化创建时间
        if (category.getCreateTime() != null) {
            dto.setCreateTime(category.getCreateTime().format(formatter));
        }
        
        return dto;
    }
} 