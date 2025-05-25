package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;

@TableName("categories")
public class Category {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("parent_id")
    private Long parentId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<Category> children;

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public List<Category> getChildren() {
        return children;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }
} 