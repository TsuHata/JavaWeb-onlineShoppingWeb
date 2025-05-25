import request from '../utils/request'

// 商家 - 获取商品列表
export const getMerchantProducts = (page: number, size: number, status?: string) => {
  return request({
    url: '/api/merchant/products',
    method: 'get',
    params: { page, size, status }
  })
}

// 商家 - 创建商品
export const createProduct = (data: any) => {
  return request({
    url: '/api/merchant/products',
    method: 'post',
    data
  })
}

// 商家 - 更新商品
export const updateProduct = (id: number, data: any) => {
  return request({
    url: `/api/merchant/products/${id}`,
    method: 'put',
    data
  })
}

// 商家 - 获取商品详情
export const getMerchantProductDetail = (id: number) => {
  return request({
    url: `/api/merchant/products/${id}`,
    method: 'get'
  })
}

// 商家 - 删除商品
export const deleteProduct = (id: number) => {
  return request({
    url: `/api/merchant/products/${id}`,
    method: 'delete'
  })
}

// 商家 - 上传商品图片
export const uploadProductImage = (file: File) => {
  console.log('准备上传文件:', file.name, '大小:', (file.size / 1024).toFixed(2) + 'KB');
  
  const formData = new FormData();
  formData.append('file', file);
  
  return request({
    url: '/api/merchant/products/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData,
    // 允许跨站点访问控制
    withCredentials: true,
    // 添加进度处理
    onUploadProgress: (progressEvent: { loaded: number; total: number }) => {
      const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total);
      console.log(`上传进度: ${percentage}%`);
    }
  }).then(response => {
    console.log('上传API响应:', response);
    return response;
  }).catch(error => {
    console.error('上传API错误:', error);
    throw error;
  });
}

// 管理员 - 获取所有商品列表（支持多种筛选条件）
export const getAllProducts = (
  page: number, 
  size: number, 
  status?: string,
  merchantId?: number,
  categoryId?: number,
  minPrice?: number,
  maxPrice?: number,
  minStock?: number,
  maxStock?: number,
  startDate?: string,
  endDate?: string,
  keyword?: string,
  sortBy?: string,
  sortDirection?: string
) => {
  return request({
    url: '/api/admin/products',
    method: 'get',
    params: { 
      page, 
      size, 
      status,
      merchantId,
      categoryId,
      minPrice,
      maxPrice,
      minStock,
      maxStock,
      startDate,
      endDate,
      keyword,
      sortBy,
      sortDirection
    }
  })
}

// 管理员 - 获取待审核商品
export const getPendingProducts = (
  page: number, 
  size: number,
  merchantId?: number,
  categoryId?: number,
  keyword?: string
) => {
  return request({
    url: '/api/admin/products/pending',
    method: 'get',
    params: { 
      page, 
      size,
      merchantId,
      categoryId,
      keyword
    }
  })
}

// 管理员 - 审核商品
export const auditProduct = (data: any) => {
  return request({
    url: '/api/admin/products/audit',
    method: 'post',
    data
  })
}

// 管理员 - 更新商品
export const adminUpdateProduct = (id: number, data: any) => {
  return request({
    url: `/api/admin/products/${id}`,
    method: 'put',
    data
  })
}

// 管理员 - 删除商品
export const adminDeleteProduct = (id: number) => {
  return request({
    url: `/api/admin/products/${id}`,
    method: 'delete'
  })
}

// 管理员 - 获取商品详情
export const getAdminProductDetail = (id: number) => {
  return request({
    url: `/api/admin/products/${id}`,
    method: 'get'
  })
}

// 公共 - 获取已审核商品列表
export const getProducts = (page: number, size: number) => {
  return request({
    url: '/api/products',
    method: 'get',
    params: { page, size }
  })
}

// 公共 - 获取商品详情
export const getProductById = (id: number) => {
  return request({
    url: `/api/products/${id}`,
    method: 'get',
    transformResponse: [(data: any) => {
      // 尝试解析JSON数据
      if (typeof data === 'string') {
        try {
          return JSON.parse(data);
        } catch (e) {
          console.error('解析商品数据失败:', e);
          return data;
        }
      }
      return data;
    }]
  }).then(response => {
    // 检查响应格式
    console.log('商品详情响应:', response);
    return response;
  }).catch(error => {
    console.error('获取商品详情失败:', error);
    throw error;
  });
}

// 公共 - 根据分类获取商品
export const getProductsByCategory = (categoryId: number, page: number, size: number) => {
  return request({
    url: `/api/products/category/${categoryId}`,
    method: 'get',
    params: { page, size }
  })
}

// 公共 - 搜索商品
export const searchProducts = (keyword: string, page: number, size: number) => {
  return request({
    url: '/api/products/search',
    method: 'get',
    params: { keyword, page, size }
  })
} 