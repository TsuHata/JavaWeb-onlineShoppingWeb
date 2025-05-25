package com.example.auth.model.dto;

import java.util.List;

public class CategoryDTO {
    private Long id;
    private String name;
    private Long parentId;
    private String createTime;
    private List<CategoryDTO> children;

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

    public String getCreateTime() {
        return createTime;
    }

    public List<CategoryDTO> getChildren() {
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

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setChildren(List<CategoryDTO> children) {
        this.children = children;
    }
} 