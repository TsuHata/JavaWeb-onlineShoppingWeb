package com.example.auth.common.util;

import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Token工具类，用于在请求过程中获取当前用户信息
 */
@Component
public class TokenUtil {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户ID
     * @return 当前用户ID，如果未登录则返回null
     */
    public Long getUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        return jwtUtil.getUserIdFromToken(token);
                    }
                }
            }
        } catch (Exception e) {
            // 记录错误但不抛出，返回null表示未获取到用户ID
            System.err.println("获取当前用户ID失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 检查当前用户是否拥有指定角色
     * @param roleName 角色名称
     * @return 是否拥有该角色
     */
    public boolean hasRole(String roleName) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        // 使用jwtUtil获取用户角色，这里假设JwtUtil有方法可以获取用户角色
                        // 如果没有这个方法，需要在JwtUtil中添加
                        return jwtUtil.hasRole(token, roleName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("检查用户角色失败: " + e.getMessage());
        }
        return false;
    }
} 