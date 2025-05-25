<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { onMounted, watch, onUnmounted } from 'vue'
import { useUserStore } from './stores/user'
import notificationService from './services/notification'

const userStore = useUserStore()
const router = useRouter()

// 初始化通知服务 - 对所有用户通用
const initNotifications = async () => {
  console.log('开始初始化通知服务');
  
  // 移除商家身份限制，允许所有用户接收通知
  // if (userStore.isMerchant) {
  const result = await notificationService.init();
  console.log('通知服务初始化结果:', result);
  // }
}

// 用户登录状态变更时，初始化通知服务
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    // 延迟一点执行，等待用户角色加载完成
    setTimeout(() => {
      initNotifications();
    }, 1000);
  }
});

// 添加页面可见性变化监听
const handleVisibilityChange = () => {
  console.log('页面可见性变化:', document.visibilityState);
  if (document.visibilityState === 'visible') {
    // 页面变为可见时，清除当前活跃会话ID，确保可以收到所有通知
    localStorage.removeItem('activeConversationId');
  }
};

// 组件挂载时初始化
onMounted(() => {
  // 如果用户已登录，初始化通知服务
  if (userStore.isLoggedIn) {
    initNotifications();
  }
  
  // 添加页面可见性变化监听
  document.addEventListener('visibilitychange', handleVisibilityChange);
});

// 组件卸载时移除监听
onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange);
});

onMounted(async () => {
  try {
    // 确保在应用启动时初始化认证
    const success = await userStore.initializeAuth()
    if (success) {
      console.log('认证初始化完成，用户编号:', userStore.user.userNumber)
    } else {
      console.log('认证初始化失败或用户未登录')
    }
  } catch (error) {
    console.error('认证初始化过程中发生错误:', error)
  }
})

// 路由变化处理
const handleRouteChange = (to: any) => {
  console.log('路由变化:', to.path);
  
  // 如果不是聊天相关页面，清除activeConversationId
  if (!to.path.includes('/chat') && !to.path.endsWith('/chat') && !to.path.includes('/messages')) {
    console.log('离开聊天页面，清除activeConversationId');
    localStorage.removeItem('activeConversationId');
  }
}

// 监听路由变化
watch(() => router.currentRoute.value, (to) => {
  handleRouteChange(to);
}, { immediate: true });
</script>

<template>
  <RouterView />
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden; /* 防止整个页面滚动 */
  position: fixed; /* 确保页面不会滚动 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
  width: 100%;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
}

/* 全局样式 */
.page-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  color: #333;
}

.card-shadow {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.text-center {
  text-align: center;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}
</style>
