package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.model.dto.DashboardStats;
import com.example.auth.model.dto.MerchantDashboardDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    private DashboardStats.SystemInfo getSystemInfo() {
        DashboardStats.SystemInfo systemInfo = new DashboardStats.SystemInfo();
        Runtime runtime = Runtime.getRuntime();
        
        systemInfo.setJavaVersion(System.getProperty("java.version"));
        systemInfo.setOsName(System.getProperty("os.name"));
        systemInfo.setOsVersion(System.getProperty("os.version"));
        systemInfo.setTotalMemory(runtime.totalMemory());
        systemInfo.setFreeMemory(runtime.freeMemory());
        systemInfo.setAvailableProcessors(runtime.availableProcessors());
        
        return systemInfo;
    }

    private List<DashboardStats.UserInfo> getUserList() {
        List<DashboardStats.UserInfo> userList = new ArrayList<>();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        
        for (User user : users) {
            DashboardStats.UserInfo userInfo = new DashboardStats.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            
            // 查询用户角色
            List<Role> roles = roleMapper.findRolesByUserId(user.getId());
            List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
            
            userInfo.setRoles(roleNames);
            userInfo.setCreateTime(user.getCreateTime() != null ? 
                    user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
                    "未知");
            userList.add(userInfo);
        }
        return userList;
    }

    @Override
    public DashboardStats getFullDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        // 获取用户总数
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        stats.setTotalUsers(totalUsers);
        
        // 获取角色分布
        Map<String, Long> roleDistribution = new HashMap<>();
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<>());
        
        for (Role role : roles) {
            // 查询具有该角色的用户数量
            Long count = (long) userMapper.findByRoleId(role.getId()).size();
            roleDistribution.put(role.getName(), count);
        }
        stats.setRoleDistribution(roleDistribution);
        
        // 获取系统信息
        stats.setSystemInfo(getSystemInfo());
        
        // 获取用户列表
        stats.setUserList(getUserList());
        
        return stats;
    }

    @Override
    public DashboardStats getBasicDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        // 普通用户不显示用户统计
        stats.setTotalUsers(0L);
        
        // 不包含角色分布
        stats.setRoleDistribution(new HashMap<>());
        
        // 不包含用户列表
        stats.setUserList(new ArrayList<>());
        
        // 只返回系统信息
        stats.setSystemInfo(getSystemInfo());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getUserCreationTrend() {
        // 获取所有用户
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<>());
        
        // 计算近7天的日期范围
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // 7天包括今天
        
        // 初始化日期和用户数量数组
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        // 日期格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        
        // 为过去7天每一天创建日期并初始化计数为0
        Map<LocalDate, Integer> dateCountMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dateCountMap.put(date, 0);
            dates.add(date.format(formatter));
        }
        
        // 统计每天创建的用户数量
        for (User user : allUsers) {
            LocalDateTime createTime = user.getCreateTime();
            if (createTime != null) {
                LocalDate createDate = createTime.toLocalDate();
                if (!createDate.isBefore(startDate) && !createDate.isAfter(today)) {
                    // 如果创建日期在近7天内，增加对应日期的计数
                    dateCountMap.put(createDate, dateCountMap.getOrDefault(createDate, 0) + 1);
                }
            }
        }
        
        // 收集统计结果到计数列表
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            counts.add(dateCountMap.get(date));
        }
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        
        return result;
    }

    @Override
    public MerchantDashboardDTO getMerchantDashboardStats(Long merchantId) {
        MerchantDashboardDTO stats = new MerchantDashboardDTO();
        
        // 获取USER角色
        Role userRole = roleMapper.findByName("USER");
        
        if (userRole != null) {
            // 查找所有拥有USER角色的用户
            List<User> users = userMapper.findByRoleId(userRole.getId());
            
            // 设置用户总数
            stats.setUserCount(users.size());
            
            // 计算今日活跃用户数（使用状态字段代替登录时间）
            int activeTodayCount = (int) users.stream()
                .filter(user -> "active".equals(user.getStatus()))
                .count();
            stats.setActiveToday(activeTodayCount);
        } else {
            // 如果没有找到USER角色，设置为0
            stats.setUserCount(0);
            stats.setActiveToday(0);
        }
        
        // 设置待处理订单
        stats.setPendingOrders(0); // 示例数据
        
        // 设置每周活动
        stats.setWeeklyEvents(0); // 示例数据
        
        // 创建用户活动示例数据
        List<Map<String, Object>> userActivities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("userId", i + 1);
            activity.put("username", "用户" + (i + 1));
            activity.put("action", "下单");
            activity.put("time", LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            userActivities.add(activity);
        }
        stats.setUserActivities(userActivities);
        
        // 创建最近活动示例数据
        List<MerchantDashboardDTO.RecentActivity> recentActivities = new ArrayList<>();
        String[] types = {"订单", "评论", "浏览", "收藏"};
        String[] titles = {"新订单", "新评论", "用户浏览", "用户收藏"};
        
        for (int i = 0; i < 4; i++) {
            MerchantDashboardDTO.RecentActivity activity = new MerchantDashboardDTO.RecentActivity();
            activity.setType(types[i]);
            activity.setTitle(titles[i]);
            activity.setContent("用户进行了" + types[i] + "操作");
            activity.setTime(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            recentActivities.add(activity);
        }
        stats.setRecentActivities(recentActivities);
        
        return stats;
    }
} 