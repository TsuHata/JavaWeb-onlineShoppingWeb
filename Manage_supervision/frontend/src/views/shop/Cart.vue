<template>
  <div class="cart-container">
    <div class="page-header">
      <h1>购物车</h1>
      <div class="header-actions">
        <el-button v-if="cartStore.totalItems > 0" type="danger" plain @click="clearCart">
          清空购物车
        </el-button>
      </div>
    </div>
    
    <div v-if="cartStore.cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车空空如也">
        <el-button type="primary" @click="goToProducts">去购物</el-button>
      </el-empty>
    </div>
    
    <div v-else class="cart-content">
      <div class="cart-items">
        <el-card shadow="never" class="cart-card">
          <template #header>
            <div class="cart-header">
              <el-checkbox 
                v-model="allSelected" 
                @change="handleSelectAllChange"
                :indeterminate="isIndeterminate"
              >
                全选
              </el-checkbox>
              <span class="cart-header-count">共 {{ cartStore.totalItems }} 件商品</span>
            </div>
          </template>
          
          <div class="cart-item" v-for="item in cartStore.cartItems" :key="item.id">
            <el-checkbox v-model="item.selected" @change="updateSelectedState"></el-checkbox>
            
            <div class="cart-item-image">
              <el-image
                :src="formatImageUrl(item.imageUrl)"
                fit="cover"
                :preview-src-list="[formatImageUrl(item.imageUrl)]"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            
            <div class="cart-item-info">
              <div class="cart-item-name">{{ item.name }}</div>
              <div class="cart-item-price">¥{{ item.price }}</div>
            </div>
            
            <div class="cart-item-actions">
              <el-input-number 
                v-model="item.quantity" 
                :min="1" 
                :max="item.stock" 
                size="small"
                @change="(val) => handleQuantityChange(item.id, val)"
              />
              
              <el-button 
                type="danger" 
                plain 
                size="small" 
                @click="removeItem(item.id)"
                class="remove-btn"
              >
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
      
      <div class="cart-summary">
        <el-card shadow="never" class="summary-card">
          <h3>订单摘要</h3>
          
          <div class="summary-item">
            <span>商品总价</span>
            <span class="price">¥{{ getSelectedTotal.toFixed(2) }}</span>
          </div>
          
          <div class="summary-item">
            <span>运费</span>
            <span>免运费</span>
          </div>
          
          <div class="summary-total">
            <span>总计</span>
            <span class="total-price">¥{{ getSelectedTotal.toFixed(2) }}</span>
          </div>
          
          <el-button 
            type="primary" 
            :disabled="getSelectedItems.length === 0"
            class="checkout-btn" 
            @click="checkout"
          >
            去结算({{ getSelectedItems.length }})
          </el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useCartStore } from '../../stores/cart'

const router = useRouter()
const cartStore = useCartStore()

// 为每个商品添加selected属性
const initializeSelectedState = () => {
  cartStore.cartItems.forEach(item => {
    if (item.selected === undefined) {
      item.selected = true;
    }
  });
}

// 全选状态
const allSelected = ref(true);
const isIndeterminate = ref(false);

// 计算全选状态
const updateSelectedState = () => {
  const selectedCount = cartStore.cartItems.filter(item => item.selected).length;
  allSelected.value = selectedCount === cartStore.cartItems.length && selectedCount > 0;
  isIndeterminate.value = selectedCount > 0 && selectedCount < cartStore.cartItems.length;
}

// 全选/取消全选
const handleSelectAllChange = (val) => {
  cartStore.cartItems.forEach(item => {
    item.selected = val;
  });
}

// 获取选中的商品
const getSelectedItems = computed(() => {
  return cartStore.cartItems.filter(item => item.selected);
});

// 计算选中商品的总价
const getSelectedTotal = computed(() => {
  return getSelectedItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity);
  }, 0);
});

// 修改商品数量
const handleQuantityChange = (id: number, quantity: number) => {
  cartStore.updateQuantity(id, quantity);
}

// 移除商品
const removeItem = (id: number) => {
  ElMessageBox.confirm(
    '确定要从购物车中移除此商品吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    cartStore.removeFromCart(id);
    updateSelectedState();
  }).catch(() => {});
}

// 清空购物车
const clearCart = () => {
  ElMessageBox.confirm(
    '确定要清空购物车吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    cartStore.clearCart();
  }).catch(() => {});
}

// 去购物页面
const goToProducts = () => {
  router.push('/shop/products');
}

// 结算
const checkout = () => {
  if (getSelectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品');
    return;
  }
  
  // 跳转到结算页面
  router.push('/shop/checkout');
}

// 处理图片URL
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

onMounted(() => {
  initializeSelectedState();
  updateSelectedState();
});
</script>

<style scoped>
.cart-container {
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

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.cart-content {
  display: flex;
  gap: 20px;
}

.cart-items {
  flex: 1;
}

.cart-summary {
  width: 300px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-header-count {
  color: #909399;
  font-size: 14px;
}

.cart-item {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  align-items: center;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-image {
  width: 80px;
  height: 80px;
  margin: 0 15px;
  overflow: hidden;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.cart-item-info {
  flex: 1;
  padding: 0 15px;
}

.cart-item-name {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.cart-item-price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: bold;
}

.cart-item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  min-width: 120px;
}

.summary-card {
  padding: 10px;
}

.summary-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  color: #303133;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  color: #606266;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  margin: 20px 0;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  font-weight: bold;
  color: #303133;
}

.price, .total-price {
  color: #f56c6c;
}

.total-price {
  font-size: 18px;
}

.checkout-btn {
  width: 100%;
  margin-top: 10px;
}

.remove-btn {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .cart-content {
    flex-direction: column;
  }
  
  .cart-summary {
    width: 100%;
  }
}
</style> 