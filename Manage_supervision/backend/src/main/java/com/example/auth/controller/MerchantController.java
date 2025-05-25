package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.dto.UserDetailDTO;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling merchant-related requests
 */
@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all users list
     */
    @GetMapping("/users")
    @RequireRole("MERCHANT")
    public ResponseEntity<?> getAllUsers() {
        logger.info("Merchant requested all users list");
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Failed to get users list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get users list: " + e.getMessage()));
        }
    }

    /**
     * Get users assigned to merchant
     */
    @GetMapping("/users/assigned")
    @RequireRole("MERCHANT")
    public ResponseEntity<?> getAssignedUsers(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long merchantId = jwtUtil.getUserIdFromToken(token);
            logger.info("Merchant requested their assigned users list, ID: {}", merchantId);
            
            List<UserDTO> users = userService.getUsersByMerchant(merchantId);
            if (!users.isEmpty()) {
                logger.info("用户数据示例: id={}, name={}, userNumber={}", 
                            users.get(0).getId(), 
                            users.get(0).getRealName(), 
                            users.get(0).getUserNumber());
            } else {
                logger.info("未找到分配给商家ID: {}的用户", merchantId);
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Failed to get assigned users list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get assigned users list: " + e.getMessage()));
        }
    }

    /**
     * Get user details
     */
    @GetMapping("/users/{id}")
    @RequireRole("MERCHANT")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        logger.info("Merchant requested user details, ID: {}", id);
        try {
            UserDetailDTO user = userService.getUserDetails(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Failed to get user details", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get user details: " + e.getMessage()));
        }
    }

    /**
     * Update user status
     */
    @PutMapping("/users/{id}/status")
    @RequireRole("MERCHANT")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        String status = statusData.get("status");
        logger.info("Merchant requested to update user status, ID: {}, New status: {}", id, status);
        
        if (status == null || (!status.equals("active") && !status.equals("inactive"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status value, must be 'active' or 'inactive'"));
        }
        
        try {
            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "User status has been updated to: " + status,
                    "status", status
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to update user status, user may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to update user status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update user status: " + e.getMessage()));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/users/{id}")
    @RequireRole("MERCHANT")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Merchant requested to delete user, ID: {}", id);
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "User has been successfully deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete user, user may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete user", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete user: " + e.getMessage()));
        }
    }
} 