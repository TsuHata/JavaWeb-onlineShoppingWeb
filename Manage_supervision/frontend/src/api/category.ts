import request from '../utils/request'

const API_BASE_URL = '/api'

// 获取所有分类列表
export const getCategories = async () => {
  try {
    const response = await request({
      url: `${API_BASE_URL}/categories/all`,
      method: 'get'
    });
    return response;
  } catch (error) {
    console.error('获取分类列表失败:', error);
    throw error;
  }
}

// 获取分类树
export const getCategoriesTree = async (parentId?: number) => {
  console.log('获取分类树, 父ID:', parentId);
  
  const url = parentId 
    ? `${API_BASE_URL}/categories?parentId=${parentId}` 
    : `${API_BASE_URL}/categories`;
    
  try {
    const response = await request({
      url: url,
      method: 'get'
    });
    console.log('分类树原始数据:', response);
    
    // 处理各种可能的响应格式
    let categoriesData: any[] = [];
    
    if (Array.isArray(response)) {
      // 直接是数组
      categoriesData = response;
    } else if (response && typeof response === 'object') {
      const resp = response as any; // 类型断言为any，避免属性检查错误
      
      if (resp.data !== undefined) {
        // 有data字段的格式
        if (Array.isArray(resp.data)) {
          categoriesData = resp.data;
        } else if (resp.data && typeof resp.data === 'object') {
          // data是对象，可能还有嵌套
          if (resp.data.categories !== undefined) {
            categoriesData = resp.data.categories;
          } else if (resp.data.data && Array.isArray(resp.data.data)) {
            categoriesData = resp.data.data;
          } else {
            console.warn('无法识别的分类数据格式:', resp.data);
          }
        }
      } else if (resp.categories !== undefined) {
        // 有categories字段的格式
        categoriesData = resp.categories;
      } else if (resp.content !== undefined && Array.isArray(resp.content)) {
        // 分页格式的响应
        categoriesData = resp.content;
      } else if (resp.list !== undefined && Array.isArray(resp.list)) {
        // list字段格式
        categoriesData = resp.list;
      } else {
        console.warn('无法识别的分类响应格式:', resp);
      }
    }
    
    console.log('处理后的分类数据:', categoriesData);
    
    // 如果没有分类数据，但又不是空数组，返回一个空数组避免错误
    if (!Array.isArray(categoriesData)) {
      console.warn('分类数据不是数组，重置为空数组');
      categoriesData = [];
    }
    
    return categoriesData;
  } catch (error) {
    console.error('获取分类树失败:', error);
    return [];
  }
}

// 获取单个分类详情
export const getCategoryById = (id: number) => {
  return request({
    url: `/api/categories/${id}`,
    method: 'get'
  }).then(response => {
    console.log('分类数据响应:', response);
    // 检查响应格式并确保正确返回
    if (typeof response === 'string') {
      try {
        return JSON.parse(response);
      } catch (e) {
        console.error('解析分类数据失败:', e);
        return response;
      }
    }
    return response;
  }).catch(error => {
    console.error('获取分类详情失败:', error);
    throw error;
  });
}

// 创建分类
export const createCategory = async (categoryData: any) => {
  const response = await request({
    url: `${API_BASE_URL}/admin/categories`,
    method: 'post',
    data: categoryData
  })
  return response
}

// 更新分类
export const updateCategory = async (categoryId: number, categoryData: any) => {
  const response = await request({
    url: `${API_BASE_URL}/admin/categories/${categoryId}`,
    method: 'put',
    data: categoryData
  })
  return response
}

// 删除分类
export const deleteCategory = async (categoryId: number) => {
  const response = await request({
    url: `${API_BASE_URL}/admin/categories/${categoryId}`,
    method: 'delete'
  })
  return response
}

// 批量创建分类
export const batchCreateCategories = async (categoriesData: any[]) => {
  const response = await request({
    url: `${API_BASE_URL}/admin/categories/batch`,
    method: 'post',
    data: categoriesData
  })
  return response
} 