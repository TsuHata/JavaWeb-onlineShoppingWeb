package com.example.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String username, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }
    
    public String generateToken(String username, Long userId, List<String> roles) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            if (claims.containsKey("userId")) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException e) {
                        System.err.println("Token中的userId无法转换为Long: " + userIdObj);
                        return null;
                    }
                }
            }
            
            System.err.println("Token中未找到userId字段");
            return null;
        } catch (Exception e) {
            System.err.println("解析Token中的userId时发生错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查token中是否包含特定角色
     * @param token JWT令牌
     * @param roleName 角色名称
     * @return 是否包含指定角色
     */
    @SuppressWarnings("unchecked")
    public boolean hasRole(String token, String roleName) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // 检查token中是否包含roles字段
            if (claims.containsKey("roles")) {
                Object rolesObj = claims.get("roles");
                
                // 如果roles是列表，直接检查
                if (rolesObj instanceof List) {
                    List<String> roles = (List<String>) rolesObj;
                    return roles.contains(roleName);
                }
            }
            
            // 如果没有角色信息，我们可以做一些特殊处理
            // 例如查询数据库获取用户角色，但这里为简单起见，我们返回false
            return false;
        } catch (Exception e) {
            System.err.println("解析Token中的角色信息时发生错误: " + e.getMessage());
            return false;
        }
    }
} 