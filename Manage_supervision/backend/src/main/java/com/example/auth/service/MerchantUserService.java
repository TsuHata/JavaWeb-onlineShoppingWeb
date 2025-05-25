package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.MerchantUserDTO;
import com.example.auth.model.dto.MerchantWithUsersDTO;
import com.example.auth.model.dto.UserDTO;

import java.util.List;

public interface MerchantUserService {
    
    // 获取所有商家列表（带分页）
    PageResponse<UserDTO> getAllMerchants(int page, int size, String keyword);
    
    // 获取所有用户列表（带分页）
    PageResponse<UserDTO> getAllUsers(int page, int size, String keyword);
    
    // 获取未分配给任何商家的用户列表
    List<UserDTO> getUnassignedUsers();
    
    // 获取特定商家的用户列表
    List<UserDTO> getUsersByMerchant(Long merchantId);
    
    // 批量分配用户给商家
    boolean assignUsersToMerchant(Long merchantId, List<Long> userIds);
    
    // 取消分配用户给商家
    boolean unassignUser(Long merchantId, Long userId);
    
    // 获取特定商家（包含其用户列表）
    MerchantWithUsersDTO getMerchantWithUsers(Long merchantId);
    
    // 获取商家-用户关系详情
    MerchantUserDTO getRelationDetail(Long relationId);
    
    // 判断商家是否分配给了指定用户
    boolean isMerchantAssignedToUser(Long merchantId, Long userId);
} 