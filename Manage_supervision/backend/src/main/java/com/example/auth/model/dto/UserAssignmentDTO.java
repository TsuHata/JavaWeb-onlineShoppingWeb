package com.example.auth.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserAssignmentDTO {

    private Long merchantId;
    private List<Long> userIds;

}