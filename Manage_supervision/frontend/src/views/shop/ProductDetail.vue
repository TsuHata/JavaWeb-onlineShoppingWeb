<template>
  <div class="product-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="!product" class="not-found-container">
      <el-empty description="商品不存在或已下架" />
      <el-button type="primary" @click="goBack">返回商品列表</el-button>
    </div>
    <div v-else class="product-info">
      <div class="product-header">
        <el-page-header @back="goBack" :title="product.name" />
      </div>
      
      <div class="product-content">
        <div class="product-image-container">
          <!-- 图片轮播 - 简化样式，移除card类型 -->
          <el-carousel v-if="previewImages.length > 0" height="400px" :interval="5000" indicator-position="outside" arrow="always" class="product-carousel">
            <el-carousel-item v-for="(img, index) in previewImages" :key="index">
              <div class="carousel-item-container">
                <el-image
                  :src="img"
                  fit="contain"
                  class="carousel-image"
                  :preview-src-list="previewImages"
                  :initial-index="index"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
            </el-carousel-item>
          </el-carousel>
          
          <!-- 无图片时的占位符 -->
          <div v-else class="image-placeholder">
            <el-icon><Picture /></el-icon>
          </div>
        </div>
        
        <div class="product-details">
          <h1 class="product-name">{{ product?.name || '商品名称' }}</h1>
          
          <div class="product-meta">
            <div class="product-category" v-if="category">
              <span class="label">分类:</span>
              <el-tag size="small" effect="plain">{{ category.name }}</el-tag>
            </div>
            
            <div class="product-merchant" v-if="product?.merchantName">
              <span class="label">商家:</span>
              <span>{{ product.merchantName }}</span>
              <el-button type="success" size="small" text @click="contactMerchant" class="contact-btn">
                <el-icon><ChatLineRound /></el-icon> 联系商家
              </el-button>
            </div>
          </div>
          
          <div class="product-price-box">
            <span class="price-label">价格:</span>
            <span class="product-price">¥{{ product?.price || '0.00' }}</span>
          </div>
          
          <div class="product-stock">
            <span class="label">库存:</span>
            <span :class="{ 'stock-warning': product?.stock < 10 }">
              {{ product?.stock > 0 ? `${product.stock} 件` : '已售罄' }}
            </span>
          </div>
          
          <div class="product-description-box">
            <h3>商品描述</h3>
            <div class="product-description">{{ product?.description || '暂无描述' }}</div>
          </div>
          
          <div class="product-actions">
            <el-button type="primary" :disabled="!product || product.stock <= 0" @click="addToCart">
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </el-button>
            
            <el-button type="danger" :disabled="!product || product.stock <= 0" @click="buyNow">
              立即购买
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商家聊天对话框 -->
    <el-dialog
      v-model="chatDialogVisible"
      title="联系商家"
      width="80%"
      destroy-on-close
      class="chat-dialog"
      @closed="handleChatDialogClosed"
    >
      <div class="chat-container">
        <ChatWindow
          :conversation-id="activeConversationId"
          @message-sent="handleMessageSent"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, ShoppingCart, ChatLineRound } from '@element-plus/icons-vue'
import { getProductById } from '../../api/product'
import { getCategoryById } from '../../api/category'
import { useCartStore } from '../../stores/cart'
import { useChatStore } from '../../stores/chat'
import ChatWindow from '../../components/chat/ChatWindow.vue'
import axios from '../../utils/axios'

const route = useRoute()
const router = useRouter()
const product = ref<any>(null)
const category = ref<any>(null)
const loading = ref(true)

// 使用购物车store
const cartStore = useCartStore()
// 使用聊天store
const chatStore = useChatStore()

// 聊天相关状态
const chatDialogVisible = ref(false)
const activeConversationId = ref<number | null>(null)

// 获取商品详情
const fetchProductDetail = async () => {
  const productId = parseInt(route.params.id as string)
  if (isNaN(productId)) {
    ElMessage.error('无效的商品ID')
    router.push('/shop/products')
    return
  }
  
  loading.value = true
  try {
    const response = await getProductById(productId)
    console.log('获取到商品数据:', response)
    
    // 判断response是否为axios响应对象，若是则取其data属性
    if (response && typeof response === 'object') {
      if (response.data) {
        product.value = response.data
      } else {
        product.value = response
      }
      
      // 检查返回的数据格式，尝试处理不同的响应结构
      if (typeof product.value === 'string') {
        try {
          product.value = JSON.parse(product.value)
        } catch (e) {
          console.error('解析商品数据失败:', e)
        }
      }
      
      console.log('处理后的商品数据:', product.value)
    }
    
    // 如果商品存在，获取分类信息
    if (product.value && product.value.categoryId) {
      fetchCategory(product.value.categoryId)
    }
  } catch (error) {
    console.error('获取商品详情失败', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 获取分类信息
const fetchCategory = async (categoryId: number) => {
  try {
    const response = await getCategoryById(categoryId)
    category.value = response
  } catch (error) {
    console.error('获取分类信息失败', error)
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 修改加入购物车方法
const addToCart = () => {
  if (!product.value) return;
  
  // 确保product对象格式正确
  const productToAdd = {
    id: product.value.id,
    name: product.value.name || '未命名商品',
    price: Number(product.value.price) || 0,
    stock: Number(product.value.stock) || 0,
    imageUrl: product.value.imageUrl || '',
    merchantId: product.value.merchantId || 0,
    merchantName: product.value.merchantName || '未知商家',
    description: product.value.description || ''
  };
  
  console.log('准备加入购物车的商品:', productToAdd);
  
  const success = cartStore.addToCart(productToAdd);
  if (success) {
    ElMessage.success('已加入购物车');
  }
}

// 修改立即购买方法
const buyNow = () => {
  if (!product.value) return;
  
  // 确保product对象格式正确
  const productToAdd = {
    id: product.value.id,
    name: product.value.name || '未命名商品',
    price: Number(product.value.price) || 0,
    stock: Number(product.value.stock) || 0,
    imageUrl: product.value.imageUrl || '',
    merchantId: product.value.merchantId || 0,
    merchantName: product.value.merchantName || '未知商家',
    description: product.value.description || ''
  };
  
  const success = cartStore.addToCart(productToAdd);
  if (success) {
    router.push('/shop/cart');
  }
}

// 联系商家
const contactMerchant = async () => {
  // 尝试从多个可能的字段获取商家ID
  let merchantId = product.value?.merchantId;
  
  // 如果没有merchantId，尝试从其他可能的字段获取
  if (!merchantId && product.value?.merchant) {
    merchantId = product.value.merchant.id || product.value.merchant;
  }
  
  if (!merchantId) {
    // 如果有商家名称但没有ID，尝试查询商家信息
    if (product.value?.merchantName) {
      try {
        const response = await axios.get('/api/merchants', {
          params: { name: product.value.merchantName }
        });
        if (response.data && response.data.length > 0) {
          merchantId = response.data[0].id;
        }
      } catch (error) {
        console.error('查询商家信息失败:', error);
      }
    }
  }

  if (!merchantId) {
    ElMessage.warning('无法获取商家信息');
    return;
  }

  try {
    // 初始化聊天服务
    if (!chatStore.initialized) {
      await chatStore.initChat();
    }

    // 获取或创建与商家的会话
    const conversation = await chatStore.getOrCreateConversationWithUser(merchantId);
    
    // 设置当前会话ID
    activeConversationId.value = conversation.id;
    // 同时存储到localStorage，用于通知服务判断
    localStorage.setItem('activeConversationId', conversation.id.toString());
    
    // 打开聊天对话框
    chatDialogVisible.value = true;
  } catch (error) {
    console.error('打开聊天失败:', error);
    ElMessage.error('打开聊天失败，请稍后重试');
  }
}

// 处理消息发送事件
const handleMessageSent = () => {
  // 可以添加消息发送后的额外处理逻辑
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

// 增加多图片解析函数，并添加时间戳
const parseImageUrls = (imageUrlStr: string | null | undefined): string[] => {
  if (!imageUrlStr) return [];
  
  // 如果是逗号分隔的多图片URL
  if (imageUrlStr.includes(',')) {
    // 分割并处理每个URL
    return imageUrlStr.split(',')
      .map(url => url.trim())
      .filter(url => url.length > 0)
      .map(url => {
        if (url.startsWith('/uploads/')) {
          // 添加时间戳防止缓存
          return `${url}?t=${new Date().getTime()}`;
        }
        return url;
      });
  }
  
  // 单图片情况
  return [formatImageUrl(imageUrlStr)];
}

// 在组件中添加预览图片列表数据
const previewImages = computed(() => {
  if (!product.value) return [];
  if (!product.value.imageUrl) return [];
  
  // 尝试处理图片URL
  try {
    return parseImageUrls(product.value.imageUrl);
  } catch (error) {
    console.error('解析图片URL错误:', error);
    return [];
  }
});

// 处理聊天对话框关闭
const handleChatDialogClosed = () => {
  // 清除活跃会话ID
  localStorage.removeItem('activeConversationId');
};

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container,
.not-found-container {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.product-header {
  margin-bottom: 20px;
}

.product-content {
  display: flex;
  gap: 30px;
}

@media (max-width: 768px) {
  .product-content {
    flex-direction: column;
  }
}

.product-image-container {
  flex: 0 0 45%;
  max-width: 500px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.product-carousel {
  width: 100%;
  height: 400px;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
}

.carousel-item-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  background-color: #fff;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 10px;
}

.el-carousel__item {
  background-color: #fff;
}

.el-carousel__item:hover {
  transform: none;
  transition: none;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 40px;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-name {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  color: #606266;
}

.label {
  font-weight: bold;
  margin-right: 8px;
}

.product-price-box {
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 4px;
}

.price-label {
  font-size: 16px;
  color: #606266;
  font-weight: bold;
}

.product-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
  margin-left: 8px;
}

.stock-warning {
  color: #e6a23c;
}

.product-description-box {
  margin-top: 10px;
}

.product-description-box h3 {
  margin: 0 0 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
}

.product-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.product-actions {
  margin-top: 20px;
  display: flex;
  gap: 15px;
}

/* 更改指示器样式 */
:deep(.el-carousel__indicators) {
  bottom: 15px;
}

:deep(.el-carousel__indicator) {
  padding: 12px 4px;
}

:deep(.el-carousel__button) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.7);
  border: 1px solid #ddd;
}

:deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background-color: #409eff;
}

/* 添加轮播箭头样式 */
:deep(.el-carousel__arrow) {
  background-color: rgba(255, 255, 255, 0.8);
  color: #333;
  border: 1px solid #eee;
  font-size: 14px;
  width: 36px;
  height: 36px;
}

:deep(.el-carousel__arrow:hover) {
  background-color: #fff;
}

:deep(.el-carousel__arrow--left) {
  left: 10px;
}

:deep(.el-carousel__arrow--right) {
  right: 10px;
}

/* 联系商家按钮样式 */
.contact-btn {
  margin-left: 10px;
  font-weight: normal;
}

/* 聊天对话框样式 */
.chat-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.chat-container {
  height: 70vh;
  min-height: 400px;
}
</style> 