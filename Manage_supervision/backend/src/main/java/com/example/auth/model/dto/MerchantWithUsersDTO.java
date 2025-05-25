package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class MerchantWithUsersDTO {
    private Long id;
    private String username;
    private String realName;
    private String userNumber;
    private String email;
    private String phone;
    private List<UserDTO> users;
} 