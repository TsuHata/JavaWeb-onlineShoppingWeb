package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 根据父级ID查找子分类
     */
    @Select("SELECT * FROM categories WHERE parent_id = #{parentId}")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查找所有根分类（父ID为null的分类）
     */
    @Select("SELECT * FROM categories WHERE parent_id IS NULL OR parent_id = 0")
    List<Category> findRootCategories();
} 