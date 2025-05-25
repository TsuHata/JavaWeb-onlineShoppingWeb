package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.MerchantUserDTO;
import com.example.auth.model.dto.MerchantWithUsersDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.MerchantUserRelation;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.MerchantUserMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.MerchantUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantUserServiceImpl implements MerchantUserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private MerchantUserMapper merchantUserMapper;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResponse<UserDTO> getAllMerchants(int page, int size, String keyword) {
        // 获取商家角色ID
        Role merchantRole = roleMapper.findByName("MERCHANT");
        if (merchantRole == null) {
            return new PageResponse<>(new ArrayList<>(), 0, page, size);
        }
        
        // 查询满足条件的商家数量
        Long merchantRoleId = merchantRole.getId();
        Integer total = userMapper.countByRoleAndKeyword(merchantRoleId, keyword);
        
        // 分页查询商家列表
        List<User> merchants = userMapper.findByRoleAndKeyword(merchantRoleId, keyword, (page - 1) * size, size);
        
        // 转换为DTO
        List<UserDTO> merchantDTOs = merchants.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(merchantDTOs, total, page, size);
    }

    @Override
    public PageResponse<UserDTO> getAllUsers(int page, int size, String keyword) {
        // 获取用户角色ID
        Role userRole = roleMapper.findByName("USER");
        if (userRole == null) {
            return new PageResponse<>(new ArrayList<>(), 0, page, size);
        }
        
        // 查询满足条件的用户数量
        Long userRoleId = userRole.getId();
        Integer total = userMapper.countByRoleAndKeyword(userRoleId, keyword);
        
        // 分页查询用户列表
        List<User> users = userMapper.findByRoleAndKeyword(userRoleId, keyword, (page - 1) * size, size);
        
        // 转换为DTO
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(userDTOs, total, page, size);
    }

    @Override
    public List<UserDTO> getUnassignedUsers() {
        // 获取普通用户角色ID
        Role userRole = roleMapper.findByName("USER");
        if (userRole == null) {
            return new ArrayList<>();
        }
        
        // 获取未分配商家的用户ID列表
        List<Long> unassignedUserIds = merchantUserMapper.findUnassignedUserIdsByRoleId(userRole.getId());
        
        if (unassignedUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询用户详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, unassignedUserIds);
        List<User> users = userMapper.selectList(queryWrapper);
        
        // 转换为DTO
        return users.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByMerchant(Long merchantId) {
        // 检查商家是否存在
        User merchant = userMapper.selectById(merchantId);
        if (merchant == null) {
            return new ArrayList<>();
        }
        
        // 获取该商家的所有活跃用户ID
        List<Long> userIds = merchantUserMapper.findActiveUserIdsByMerchantId(merchantId);
        
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询用户详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIds);
        List<User> users = userMapper.selectList(queryWrapper);
        
        // 转换为DTO
        return users.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean assignUsersToMerchant(Long merchantId, List<Long> userIds) {
        User merchant = userMapper.selectById(merchantId);
        if (merchant == null) {
            return false;
        }
        
        // 验证所有用户ID是否有效
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIds);
        long count = userMapper.selectCount(queryWrapper);
        
        if (count != userIds.size()) {
            return false;
        }
        
        for (Long userId : userIds) {
            // 检查该用户是否已分配给商家
            boolean exists = merchantUserMapper.existsByMerchantIdAndUserId(merchantId, userId);
            if (!exists) {
                MerchantUserRelation relation = new MerchantUserRelation();
                relation.setMerchantId(merchantId);
                relation.setUserId(userId);
                relation.setStatus("active");
                relation.setAssignTime(LocalDateTime.now());
                merchantUserMapper.insert(relation);
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean unassignUser(Long merchantId, Long userId) {
        // 检查商家和用户是否存在
        User merchant = userMapper.selectById(merchantId);
        User user = userMapper.selectById(userId);
        
        if (merchant == null || user == null) {
            return false;
        }
        
        // 查询关系记录
        MerchantUserRelation relation = merchantUserMapper.findByMerchantIdAndUserId(merchantId, userId);
        
        if (relation != null) {
            relation.setStatus("inactive");
            merchantUserMapper.updateById(relation);
            return true;
        }
        
        return false;
    }

    @Override
    public MerchantWithUsersDTO getMerchantWithUsers(Long merchantId) {
        User merchant = userMapper.selectById(merchantId);
        if (merchant == null) {
            return null;
        }
        
        // 获取该商家的所有活跃用户ID
        List<Long> userIds = merchantUserMapper.findActiveUserIdsByMerchantId(merchantId);
        
        // 查询用户详细信息
        List<User> users = new ArrayList<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(User::getId, userIds);
            users = userMapper.selectList(queryWrapper);
        }
        
        MerchantWithUsersDTO dto = new MerchantWithUsersDTO();
        dto.setId(merchant.getId());
        dto.setUsername(merchant.getUsername());
        dto.setRealName(merchant.getRealName());
        dto.setUserNumber(merchant.getUserNumber());
        dto.setEmail(merchant.getEmail());
        dto.setPhone(merchant.getPhone());
        dto.setUsers(users.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    public MerchantUserDTO getRelationDetail(Long relationId) {
        MerchantUserRelation relation = merchantUserMapper.selectById(relationId);
        if (relation == null) {
            return null;
        }
        
        // 补充商家和用户信息
        User merchant = userMapper.selectById(relation.getMerchantId());
        User user = userMapper.selectById(relation.getUserId());
        
        relation.setMerchant(merchant);
        relation.setUser(user);
        
        return convertToMerchantUserDTO(relation);
    }

    @Override
    public boolean isMerchantAssignedToUser(Long merchantId, Long userId) {
        // 检查商家和用户是否存在
        User merchant = userMapper.selectById(merchantId);
        User user = userMapper.selectById(userId);
        
        if (merchant == null || user == null) {
            return false;
        }
        
        // 查询是否存在活跃的商家-用户关系
        return merchantUserMapper.existsByMerchantIdAndUserId(merchantId, userId);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setUserNumber(user.getUserNumber());
        dto.setAvatar(user.getAvatar());
        return dto;
    }

    private MerchantUserDTO convertToMerchantUserDTO(MerchantUserRelation relation) {
        MerchantUserDTO dto = new MerchantUserDTO();
        dto.setId(relation.getId());
        
        if (relation.getMerchant() != null) {
            dto.setMerchantId(relation.getMerchant().getId());
            dto.setMerchantName(relation.getMerchant().getRealName() != null 
                    ? relation.getMerchant().getRealName() 
                    : relation.getMerchant().getUsername());
        } else {
            dto.setMerchantId(relation.getMerchantId());
        }
        
        if (relation.getUser() != null) {
            dto.setUserId(relation.getUser().getId());
            dto.setUserName(relation.getUser().getRealName() != null 
                    ? relation.getUser().getRealName() 
                    : relation.getUser().getUsername());
        } else {
            dto.setUserId(relation.getUserId());
        }
        
        dto.setStatus(relation.getStatus());
        dto.setAssignTime(relation.getAssignTime() != null 
                ? relation.getAssignTime().format(formatter) 
                : null);
        
        return dto;
    }
} 