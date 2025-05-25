package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.UserAssignmentDTO;
import com.example.auth.model.dto.MerchantWithUsersDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.MerchantUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminMerchantUserController {

    @Autowired
    private MerchantUserService merchantUserService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    // 获取所有商家列表（带分页）
    @GetMapping("/merchants")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<UserDTO>> getAllMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<UserDTO> response = merchantUserService.getAllMerchants(page, size, keyword);
        return ResponseEntity.ok(response);
    }

    // 获取所有用户列表（带分页）
    @GetMapping("/merchant-users")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<UserDTO> response = merchantUserService.getAllUsers(page, size, keyword);
        return ResponseEntity.ok(response);
    }

    // 获取未分配用户列表
    @GetMapping("/users/unassigned")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getUnassignedUsers() {
        List<UserDTO> users = merchantUserService.getUnassignedUsers();
        return ResponseEntity.ok(users);
    }

    // 获取特定商家的用户列表
    @GetMapping("/merchants/{merchantId}/users")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getUsersByMerchant(@PathVariable Long merchantId) {
        List<UserDTO> users = merchantUserService.getUsersByMerchant(merchantId);
        return ResponseEntity.ok(users);
    }
    
    // 获取特定用户的商家列表
    @GetMapping("/merchants/user/{userId}")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getMerchantsByUser(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        Role merchantRole = roleMapper.findByName("MERCHANT");
        if (merchantRole == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        // 获取与该用户相关的商家
        List<User> merchants = userMapper.findByRoleId(merchantRole.getId());
        
        // 转换为DTO并过滤出已经分配给该用户的商家
        List<UserDTO> merchantDTOs = new ArrayList<>();
        for (User merchant : merchants) {
            if (merchantUserService.isMerchantAssignedToUser(merchant.getId(), userId)) {
                UserDTO merchantDTO = new UserDTO();
                merchantDTO.setId(merchant.getId());
                merchantDTO.setUsername(merchant.getUsername());
                merchantDTO.setRealName(merchant.getRealName());
                merchantDTO.setUserNumber(merchant.getUserNumber());
                merchantDTO.setEmail(merchant.getEmail());
                merchantDTO.setPhone(merchant.getPhone());
                merchantDTOs.add(merchantDTO);
            }
        }
        
        return ResponseEntity.ok(merchantDTOs);
    }

    // 分配用户给商家
    @PostMapping("/merchants/assign-users")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> assignUsersToMerchant(@RequestBody UserAssignmentDTO assignmentDTO) {
        boolean success = merchantUserService.assignUsersToMerchant(
                assignmentDTO.getMerchantId(), assignmentDTO.getUserIds());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "用户分配成功" : "用户分配失败");
        
        return ResponseEntity.ok(response);
    }

    // 取消分配用户
    @PostMapping("/merchants/{merchantId}/unassign/{userId}")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> unassignUser(
            @PathVariable Long merchantId, @PathVariable Long userId) {
        
        boolean success = merchantUserService.unassignUser(merchantId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "取消分配成功" : "取消分配失败");
        
        return ResponseEntity.ok(response);
    }

    // 获取商家详情（包含用户列表）
    @GetMapping("/merchants/{merchantId}/details")
    @RequireRole("ADMIN")
    public ResponseEntity<MerchantWithUsersDTO> getMerchantDetails(@PathVariable Long merchantId) {
        MerchantWithUsersDTO merchant = merchantUserService.getMerchantWithUsers(merchantId);
        return ResponseEntity.ok(merchant);
    }
} 