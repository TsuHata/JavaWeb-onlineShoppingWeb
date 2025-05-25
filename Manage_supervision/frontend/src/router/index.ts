import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '../stores/user'
import BaseLayout from '../layouts/BaseLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: BaseLayout,
    redirect: to => {
      const userStore = useUserStore()
      if (userStore.isAdmin) {
        return '/admin/dashboard'
      } else if (userStore.isMerchant) {
        return '/merchant/profile'
      } else {
        return '/shop/products'
      }
    },
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { 
          title: '个人信息',
          requiresAuth: true 
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: {
          title: '账号设置',
          requiresAuth: true
        }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/chat/ChatPage.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/shop',
    component: BaseLayout,
    children: [
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('../views/shop/ProductList.vue'),
        meta: { 
          title: '商品列表'
        }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/shop/ProductDetail.vue'),
        meta: { 
          title: '商品详情'
        }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../views/shop/Cart.vue'),
        meta: {
          title: '购物车'
        }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('../views/shop/Checkout.vue'),
        meta: {
          title: '结算',
          requiresAuth: true
        }
      },
      {
        path: 'payment/:id',
        name: 'Payment',
        component: () => import('../views/shop/Payment.vue'),
        meta: {
          title: '订单支付',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/user',
    component: BaseLayout,
    meta: { 
      requiresAuth: true
    },
    children: [
      {
        path: 'orders',
        name: 'UserOrders',
        component: () => import('../views/user/OrderList.vue'),
        meta: { 
          title: '我的订单',
          requiresAuth: true
        }
      },
      {
        path: 'orders/:id',
        name: 'UserOrderDetail',
        component: () => import('../views/shop/Payment.vue'),
        meta: { 
          title: '订单详情',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin',
    component: BaseLayout,
    redirect: '/admin/dashboard',
    meta: { 
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { 
          title: '管理控制台',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: { 
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'merchants',
        name: 'MerchantManagement',
        component: () => import('../views/admin/MerchantManagement.vue'),
        meta: { 
          title: '商家管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('../views/admin/RoleManagement.vue'),
        meta: { 
          title: '角色管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('../views/admin/CategoryManagement.vue'),
        meta: { 
          title: '分类管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'product-audit',
        name: 'ProductAudit',
        component: () => import('../views/admin/ProductAudit.vue'),
        meta: { 
          title: '商品审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'product-management',
        name: 'AdminProductManagement',
        component: () => import('../views/admin/ProductManagement.vue'),
        meta: { 
          title: '商品管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'logs',
        name: 'SystemLogs',
        component: () => import('../views/admin/SystemLogs.vue'),
        meta: { 
          title: '系统日志',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'orders',
        name: 'AdminOrderManagement',
        component: () => import('../views/admin/OrderManagement.vue'),
        meta: { 
          title: '订单管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
  {
    path: '/merchant',
    component: BaseLayout,
    redirect: '/merchant/profile',
    meta: { 
      requiresAuth: true,
      requiresMerchant: true
    },
    children: [
      {
        path: 'profile',
        name: 'MerchantProfile',
        component: () => import('../views/merchant/Profile.vue'),
        meta: { 
          title: '商家信息',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'revenue-report',
        name: 'MerchantRevenueReport',
        component: () => import('../views/merchant/RevenueReport.vue'),
        meta: { 
          title: '收入报表',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('../views/merchant/ProductManagement.vue'),
        meta: { 
          title: '商品管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('../views/merchant/OrderManagement.vue'),
        meta: { 
          title: '订单管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'after-sale',
        name: 'MerchantAfterSale',
        component: () => import('../views/merchant/AfterSaleManagement.vue'),
        meta: { 
          title: '售后管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'orders/:id',
        name: 'MerchantOrderDetail',
        component: () => import('../views/shop/Payment.vue'),
        meta: { 
          title: '订单详情',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'chat',
        name: 'MerchantChat',
        component: () => import('../views/chat/ChatPage.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true,
          requiresMerchant: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  console.log('路由导航开始，目标路径:', to.path)
  const userStore = useUserStore()
  
  // 初始化用户认证状态
  if (!userStore.initialized) {
    console.log('用户认证状态尚未初始化，执行初始化...')
    await userStore.initializeAuth()
  }
  
  // 如果需要身份验证
  if (to.meta.requiresAuth) {
    console.log('目标路径需要登录权限')
    
    // 检查是否已登录
    if (!userStore.isLoggedIn) {
      console.log('用户未登录，重定向到登录页面')
      next('/login')
      return
    }
    
    // 检查特定角色要求之前，强制刷新用户信息以确保权限是最新的
    if (to.meta.requiresAdmin || to.meta.requiresMerchant) {
      console.log('页面需要特定角色权限，刷新用户信息...')
      try {
        // 尝试刷新用户信息，但不强制刷新以避免可能的循环
        // 只有当实际角色与预期角色不匹配时才强制刷新
        let needsForceRefresh = false
        
        if (to.meta.requiresAdmin && !userStore.isAdmin) {
          console.log('需要管理员权限但当前不是管理员，尝试强制刷新')
          needsForceRefresh = true
        } else if (to.meta.requiresMerchant && !userStore.isMerchant) {
          console.log('需要商家权限但当前不是商家，尝试强制刷新')
          needsForceRefresh = true
        }
        
        if (needsForceRefresh) {
          await userStore.fetchUserInfo(true)
        }
      } catch (error) {
        console.error('刷新用户信息失败:', error)
      }
    }
    
    // 检查管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      console.log('需要管理员权限，但用户不是管理员，重定向到首页')
      next('/chat')
      return
    }
    
    // 检查商家权限
    if (to.meta.requiresMerchant && !userStore.isMerchant) {
      console.log('需要商家权限，但用户不是商家，重定向到首页')
      next('/chat')
      return
    }
  }
  
  // 如果用户已登录且访问登录页，根据角色重定向到对应页面
  if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    if (userStore.isAdmin) {
      next('/admin/dashboard')
    } else if (userStore.isMerchant) {
      next('/merchant/users')
    } else {
      next('/shop/products')
    }
    return
  }
  
  // 设置页面标题
  document.title = `${to.meta.title || '首页'} - 商城管理系统`
  
  // 放行路由
  console.log('路由检查通过，允许导航到:', to.path)
  next()
})

export default router