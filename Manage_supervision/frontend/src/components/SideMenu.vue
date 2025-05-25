<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapsed"
    :collapse-transition="false"
    class="side-menu"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
  >
    <!-- 管理员菜单 -->
    <template v-if="userStore.isAdmin">
      <el-menu-item index="/admin/dashboard" @click="handleRoute('/admin/dashboard')">
        <el-icon><Monitor /></el-icon>
        <template #title>
          <span>管理控制台</span>
        </template>
      </el-menu-item>

      <el-sub-menu index="admin-management">
        <template #title>
          <el-icon><Management /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/admin/users" @click="handleRoute('/admin/users')">
          <el-icon><Avatar /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/merchants" @click="handleRoute('/admin/merchants')">
          <el-icon><ShoppingBag /></el-icon>
          <span>商家管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/roles" @click="handleRoute('/admin/roles')">
          <el-icon><Lock /></el-icon>
          <span>角色管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories" @click="handleRoute('/admin/categories')">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/product-audit" @click="handleRoute('/admin/product-audit')">
          <el-icon><Goods /></el-icon>
          <span>商品审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/product-management" @click="handleRoute('/admin/product-management')">
          <el-icon><ShoppingCart /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders" @click="handleRoute('/admin/orders')">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 商家菜单 -->
    <template v-else-if="userStore.isMerchant">
      <el-menu-item index="/merchant/chat" @click="handleRoute('/merchant/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/merchant/profile" @click="handleRoute('/merchant/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>商家信息</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/merchant/revenue-report" @click="handleRoute('/merchant/revenue-report')">
        <el-icon><DataLine /></el-icon>
        <template #title>
          <span>收入报表</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/merchant/products" @click="handleRoute('/merchant/products')">
        <el-icon><Goods /></el-icon>
        <template #title>
          <span>商品管理</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/merchant/orders" @click="handleRoute('/merchant/orders')">
        <el-icon><Document /></el-icon>
        <template #title>
          <span>订单管理</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/merchant/after-sale" @click="handleRoute('/merchant/after-sale')">
        <el-icon><Service /></el-icon>
        <template #title>
          <span>售后管理</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 用户菜单 -->
    <template v-else>
      <el-menu-item index="/shop/products" @click="handleRoute('/shop/products')">
        <el-icon><ShoppingCart /></el-icon>
        <template #title>
          <span>商品浏览</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/user/orders" @click="handleRoute('/user/orders')">
        <el-icon><Document /></el-icon>
        <template #title>
          <span>我的订单</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/chat" @click="handleRoute('/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/profile" @click="handleRoute('/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>个人信息</span>
        </template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import {
  Monitor,
  User,
  UserFilled,
  Setting,
  Management,
  Avatar,
  Lock,
  Document,
  List,
  Folder,
  ChatDotRound,
  ShoppingBag,
  Goods,
  ShoppingCart,
  Service,
  DataLine
} from '@element-plus/icons-vue'

const props = defineProps<{
  isCollapsed: boolean
}>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

const handleRoute = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.side-menu {
  border-right: none;
  user-select: none;
}

.side-menu:not(.el-menu--collapse) {
  width: 210px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item) {
  &:hover {
    background-color: #263445 !important;
  }
  
  &.is-active {
    background-color: #1890ff !important;
    color: #fff !important;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: #fff;
    }
  }
}

:deep(.el-sub-menu__title) {
  &:hover {
    background-color: #263445 !important;
  }
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  width: 16px;
  height: 16px;
  margin-right: 16px;
  font-size: 16px;
}

/* 折叠时的图标样式 */
.el-menu--collapse {
  :deep(.el-sub-menu__title) {
    span {
      height: 0;
      width: 0;
      overflow: hidden;
      visibility: hidden;
      display: inline-block;
    }
  }
}
</style> 