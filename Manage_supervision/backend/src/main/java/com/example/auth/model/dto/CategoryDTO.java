package com.example.auth.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CategoryDTO {
    // Setters
    // Getters
    private Long id;
    private String name;
    private Long parentId;
    private String createTime;
    private List<CategoryDTO> children;

}