// 该文件是为了兼容性而创建的，实际上重新导出student.ts中的内容

import { getStudents as _getStudents } from './student';
import axios from '../utils/axios';

// 将getStudents作为getAllStudents导出，以便兼容旧代码
export const getAllStudents = _getStudents;

// 直接在此处定义Student类型，而不是导入StudentDTO
export interface Student {
  id: number;
  name: string;
  studentId?: string;
  userNumber: string; // 添加userNumber字段作为学号
  class?: string; // 注意：这里用class而不是className，与StudentManagement.vue中使用一致
  email: string;
  phone: string;
  status: string;
  lastLogin?: string;
  progress?: number;
  username?: string;
  realName?: string;
  roles?: string[];
  createTime?: string;
}

// 用户类型定义
export interface User {
  id: number;
  username: string;
  name?: string;
  realName?: string;
  userNumber?: string;
  email?: string;
  phone?: string;
  status?: string;
  createTime?: string;
  roles?: string[];
  avatar?: string;
  bio?: string;
  nickname?: string;
}

// 获取用户列表（分页）
export const getUsers = async (page = 1, size = 10, keyword?: string) => {
  try {
    // 从localStorage获取用户角色
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    let url = '/api/admin/users'; // 默认管理员API
    
    // 如果是商家角色，使用商家API
    if (userRoles.includes('MERCHANT')) {
      url = '/api/merchant/users/assigned';
    }
    
    const params: any = { page, size };
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await axios.get(url, { params });
    return response.data;
  } catch (error) {
    console.error('获取用户列表失败:', error);
    throw error;
  }
};

// 获取未分配的用户列表
export const getUnassignedUsers = async () => {
  try {
    const response = await axios.get('/api/admin/users/unassigned');
    return response.data;
  } catch (error) {
    console.error('获取未分配用户列表失败:', error);
    throw error;
  }
};

// 获取用户详情
export const getUserDetail = async (userId: number) => {
  try {
    // 从localStorage获取用户角色
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');
    console.log('当前用户角色:', userRoles);
    
    // 默认使用商家API
    const url = `/api/merchant/users/${userId}`;
    console.log('使用API路径:', url);
    
    // 保存原始错误以便在处理失败时显示
    let originalError = null;
    
    try {
      const response = await axios.get(url);
      console.log('获取用户详情成功:', response.data);
      return response.data;
    } catch (error) {
      console.warn('通过商家API获取用户详情失败，尝试使用用户列表数据:', error);
      originalError = error;
      
      // 尝试从已获取的用户列表中找到该用户
      const assignedResponse = await axios.get('/api/merchant/users/assigned');
      if (Array.isArray(assignedResponse.data)) {
        const user = assignedResponse.data.find(s => s.id === userId);
        if (user) {
          console.log('从已获取的用户列表中找到用户:', user);
          return user;
        }
      }
      
      // 如果仍未找到，抛出原始错误
      throw originalError;
    }
  } catch (error) {
    console.error('获取用户详情失败:', error);
    throw error;
  }
};

// 更新用户状态
export const updateUserStatus = async (userId: number, status: string) => {
  try {
    const response = await axios.post(`/api/admin/users/${userId}/toggle-status`);
    return true;
  } catch (error) {
    console.error('更新用户状态失败:', error);
    throw error;
  }
};

// 删除用户
export const deleteUser = async (userId: number) => {
  try {
    const response = await axios.delete(`/api/merchant/users/${userId}`);
    return response.status === 200;
  } catch (error) {
    console.error('删除用户失败:', error);
    throw error;
  }
};

// 获取商家的所有用户列表
export const getAssignedUsers = async () => {
  try {
    const response = await axios.get('/api/merchant/users/assigned');
    return response.data;
  } catch (error) {
    console.error('获取分配的用户列表失败:', error);
    throw error;
  }
}; 