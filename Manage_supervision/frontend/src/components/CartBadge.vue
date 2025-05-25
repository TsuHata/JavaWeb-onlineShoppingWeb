<template>
  <div class="cart-badge" @click="navigateToCart">
    <el-badge :value="totalItems > 0 ? totalItems : ''" :max="99" :hidden="totalItems <= 0" class="cart-badge-item">
      <el-icon class="cart-icon" :size="24">
        <ShoppingCart />
      </el-icon>
    </el-badge>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()

// 从购物车store获取商品总数
const totalItems = computed(() => cartStore.totalItems)

// 导航到购物车页面
const navigateToCart = () => {
  router.push('/shop/cart')
}
</script>

<style scoped>
.cart-badge {
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-badge:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.cart-icon {
  color: #606266;
}

.cart-badge:hover .cart-icon {
  color: #409eff;
}

.cart-badge-item :deep(.el-badge__content) {
  background-color: #f56c6c;
}
</style> 