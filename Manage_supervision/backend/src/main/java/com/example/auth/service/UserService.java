package com.example.auth.service;

import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.dto.UserDetailDTO;
import com.example.auth.model.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {
    User register(String username, String password);
    User findByUsername(String username);
    User findById(Long id);
    boolean validatePassword(User user, String password);
    void changePassword(User user, String currentPassword, String newPassword);
    void updateAvatar(User user, String avatarUrl);
    User updateProfile(User user, Map<String, String> profileData);
    
    // 用户管理相关方法
    List<UserDTO> getAllUsers();
    UserDetailDTO getUserDetails(Long id);
    boolean updateUserStatus(Long id, String status);
    boolean deleteUser(Long id);
    
    // 商家管理相关方法
    List<User> getAllMerchants();

    // 添加获取商家用户的方法
    List<UserDTO> getUsersByMerchant(Long merchantId);
} 