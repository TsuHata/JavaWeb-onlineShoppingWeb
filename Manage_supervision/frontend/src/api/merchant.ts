import axios from '../utils/axios';
import type { User } from './user';
import request from '../utils/axios'

// 获取商家列表
export const getMerchants = async (page?: number, size?: number, keyword?: string) => {
  try {
    const params: any = {};
    
    // 如果提供了分页参数，使用分页请求
    if (page !== undefined && size !== undefined) {
      params.page = page;
      params.size = size;
    }
    
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await axios.get('/api/admin/merchants', { params });
    
    // 返回列表数据，兼容分页和非分页情况
    if (page !== undefined) {
      // 分页情况，返回整个响应对象供前端处理
      return response.data;
    } else {
      // 非分页情况，直接返回数据列表
      // 检查响应格式并适配
      if (response.data.items) {
        return response.data.items;
      } else if (Array.isArray(response.data)) {
        return response.data;
      } else if (response.data.content) {
        return response.data.content;
      }
      return [];
    }
  } catch (error) {
    console.error('获取商家列表失败:', error);
    throw error;
  }
};

/**
 * 获取商家收入报表数据
 * @param timeRange 时间范围：week(近7天), month(近30天)
 */
export const getMerchantRevenueReport = (timeRange = 'week') => {
  return request({
    url: '/api/merchant/reports/revenue',
    method: 'get',
    params: { timeRange }
  })
}

/**
 * 获取商家订单状态分布
 */
export const getOrderStatusDistribution = () => {
  return request({
    url: '/api/merchant/reports/order-status',
    method: 'get'
  })
} 