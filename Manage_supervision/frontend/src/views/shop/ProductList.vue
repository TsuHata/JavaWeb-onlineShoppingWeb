<template>
  <div class="product-list-container">
    <div class="page-header">
      <h1>商品列表</h1>
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="category-filter">
      <el-radio-group v-model="selectedCategoryId" @change="handleCategoryChange">
        <el-radio :label="0" :value="0">全部</el-radio>
        <el-radio v-for="category in categories" :key="category.id" :label="category.id" :value="category.id">
          {{ category.name }}
        </el-radio>
      </el-radio-group>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="products.length === 0" class="empty-container">
      <el-empty description="暂无商品" />
    </div>
    <div v-else class="product-grid">
      <el-card
        v-for="product in products"
        :key="product.id"
        class="product-card"
        shadow="hover"
        @click="goToProductDetail(product.id)"
      >
        <div class="product-image">
          <el-image
            :src="formatImageUrl(product.imageUrl)"
            fit="cover"
            :preview-src-list="getProductPreviewImages(product)"
            alt="商品图片"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-price">¥{{ product.price }}</p>
          <p class="product-description">{{ product.description }}</p>
        </div>
      </el-card>
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Search } from '@element-plus/icons-vue'
import { getProducts, getProductsByCategory, searchProducts } from '../../api/product'
import { getCategories } from '../../api/category'

const router = useRouter()
const products = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(true)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const selectedCategoryId = ref(0) // 0表示全部分类

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    let response
    
    if (searchKeyword.value) {
      // 搜索
      response = await searchProducts(searchKeyword.value, currentPage.value, pageSize.value)
    } else if (selectedCategoryId.value > 0) {
      // 根据分类获取
      response = await getProductsByCategory(selectedCategoryId.value, currentPage.value, pageSize.value)
    } else {
      // 获取全部商品
      response = await getProducts(currentPage.value, pageSize.value)
    }
    
    console.log('API响应数据:', response)
    
    // 处理响应数据以获取正确的数组
    if (response.data && response.data.success) {
      // 处理新格式：{success: true, data: [...]}
      const actualPayload = response.data.data;
      
      if (Array.isArray(actualPayload)) {
        products.value = actualPayload;
        total.value = actualPayload.length;
      } else if (actualPayload && typeof actualPayload === 'object') {
        if (actualPayload.items !== undefined && actualPayload.total !== undefined) {
          products.value = actualPayload.items;
          total.value = actualPayload.total;
        } else if (actualPayload.content !== undefined && actualPayload.total !== undefined) {
          products.value = actualPayload.content;
          total.value = actualPayload.total;
        } else if (actualPayload.records !== undefined && actualPayload.total !== undefined) {
          products.value = actualPayload.records;
          total.value = actualPayload.total;
        } else {
          products.value = [];
          total.value = 0;
          console.error('未知的响应数据结构 (actualPayload):', actualPayload);
        }
      } else {
        products.value = [];
        total.value = 0;
        console.error('未知的响应数据类型 (actualPayload):', actualPayload);
      }
    } else {
      // 处理旧格式
      if (Array.isArray(response)) {
        products.value = response;
        total.value = response.length;
      } else if (response.data && Array.isArray(response.data)) {
        products.value = response.data;
        total.value = response.data.length;
      } else if (response.items) {
        products.value = response.items;
        total.value = response.total;
      } else if (response.content) {
        products.value = response.content;
        total.value = response.total;
      } else if (response.data && response.data.items) {
        products.value = response.data.items;
        total.value = response.data.total;
      } else if (response.data && response.data.content) {
        products.value = response.data.content;
        total.value = response.data.total;
      } else if (response.data && response.data.records) {
        products.value = response.data.records;
        total.value = response.data.total;
      } else {
        products.value = [];
        total.value = 0;
        console.error('未知的响应数据格式:', response);
      }
    }
    
    console.log('处理后的商品列表:', products.value)
  } catch (error) {
    console.error('获取商品列表失败', error)
    ElMessage.error('获取商品列表失败')
    // 设置空数组避免错误
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategories()
    console.log('分类响应数据:', response)
    
    if (response.data && response.data.success) {
      // 新格式：{success: true, data: [...]}
      categories.value = response.data.data || []
    } else if (response.data && Array.isArray(response.data)) {
      // 旧格式：{data: [...]}
      categories.value = response.data
    } else if (Array.isArray(response)) {
      // 直接返回数组
      categories.value = response
    } else {
      console.error('获取分类列表失败：未知响应格式', response)
      categories.value = []
    }
    
    console.log('处理后的分类列表:', categories.value)
  } catch (error) {
    console.error('获取分类列表失败', error)
    ElMessage.error('获取分类列表失败')
    categories.value = []
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 处理分类切换
const handleCategoryChange = () => {
  currentPage.value = 1
  fetchProducts()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchProducts()
}

// 处理每页数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchProducts()
}

// 跳转到商品详情页
const goToProductDetail = (id: number) => {
  router.push(`/shop/product/${id}`)
}

// 修改图片格式化函数，处理第一张图片并添加时间戳防止缓存
const formatImageUrl = (url: string | null | undefined) => {
  if (!url) return '/placeholder.png';
  
  // 处理多图片URL情况（逗号分隔）
  if (url.includes(',')) {
    const firstUrl = url.split(',')[0].trim();
    return formatImageUrl(firstUrl);
  }
  
  // 处理相对路径情况
  if (url.startsWith('/uploads/')) {
    // 添加时间戳防止缓存
    return `${url}?t=${new Date().getTime()}`;
  }
  
  // 其他情况返回原URL
  return url;
}

// 增加多图片解析函数
const parseImageUrls = (imageUrlStr: string | null | undefined): string[] => {
  if (!imageUrlStr) return [];
  
  // 如果是逗号分隔的多图片URL
  if (imageUrlStr.includes(',')) {
    // 分割并处理每个URL
    return imageUrlStr.split(',')
      .map(url => url.trim())
      .filter(url => url.length > 0)
      .map(url => url.startsWith('/uploads/') ? url : url);
  }
  
  // 单图片情况
  return [formatImageUrl(imageUrlStr)];
}

// 添加计算属性，为每个商品提供预览图片列表
const getProductPreviewImages = (product: any) => {
  if (!product || !product.imageUrl) return [];
  return parseImageUrls(product.imageUrl);
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.product-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  width: 300px;
}

.category-filter {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  height: 200px;
  overflow: hidden;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

.product-info {
  padding: 10px 0;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin: 0 0 10px;
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.product-description {
  margin: 0;
  color: #666;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.loading-container,
.empty-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 