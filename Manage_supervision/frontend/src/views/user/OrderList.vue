<template>
  <div class="order-list-container">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>
    
    <div class="order-content" v-loading="loading">
      <div v-if="(!orders || orders.length === 0) && !loading" class="empty-orders">
        <el-empty description="暂无订单">
          <el-button type="primary" @click="goToProducts">去购物</el-button>
        </el-empty>
      </div>
      
      <div v-else-if="orders && orders.length > 0" class="order-list">
        <el-scrollbar class="orders-scrollbar">
          <el-card v-for="order in orders" :key="order.id" shadow="never" class="order-card">
            <template #header>
              <div class="order-header">
                <div class="order-id">
                  <span class="label">订单号:</span>
                  <span class="value">{{ order.orderNumber }}</span>
                </div>
                
                <div class="order-time">
                  <span class="label">下单时间:</span>
                  <span class="value">{{ formatDate(order.createTime) }}</span>
                </div>
                
                <div class="order-status" :class="order.status">
                  {{ getStatusText(order.status) }}
                </div>
              </div>
            </template>
            
            <div class="order-items">
              <div v-for="item in order.items" :key="item.id" class="order-item">
                <div class="item-image">
                  <el-image
                    :src="formatImageUrl(item.productImageUrl)"
                    fit="cover"
                    :preview-src-list="[formatImageUrl(item.productImageUrl)]"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                
                <div class="item-info">
                  <div class="item-name">{{ item.productName }}</div>
                  <div class="item-price">¥{{ item.productPrice }}</div>
                </div>
                
                <div class="item-quantity">
                  × {{ item.quantity }}
                </div>
                
                <div class="item-subtotal">
                  <span class="amount">¥{{ item.subtotal }}</span>
                  <div class="merchant">{{ item.merchantName || '未知商家' }}</div>
                </div>
              </div>
            </div>
            
            <div class="order-footer">
              <div class="order-total">
                <span class="label">合计:</span>
                <span class="amount">¥{{ order.totalAmount }}</span>
              </div>
              
              <div class="order-actions">
                <el-button
                  v-if="order.status === 'pending'"
                  type="primary"
                  size="small"
                  @click="goToPayment(order.id)"
                >
                  去支付
                </el-button>
                
                <!-- 用户只能取消待支付的订单，取消已支付订单需由商家操作 -->                <el-button                v-if="order.status === 'pending'"                type="danger"                plain                size="small"                @click="cancelOrder(order.id)"              >                取消订单              </el-button>
                
                <el-button
                  v-if="order.status === 'shipped'"
                  type="success"
                  size="small"
                  @click="confirmReceived(order.id)"
                >
                  确认收货
                </el-button>
                
                <!-- 申请售后按钮，所有状态都可申请，但每笔订单仅可申请一次 -->
                <el-button
                  v-if="!order.afterSaleStatus"
                  type="warning"
                  plain
                  size="small"
                  @click="applyAfterSale(order.id)"
                >
                  申请售后
                </el-button>
                
                <el-button
                  type="primary"
                  plain
                  size="small"
                  @click="viewOrderDetailWithAfterSale(order)"
                >
                  订单详情
                </el-button>
                
                <el-button
                  type="success"
                  plain
                  size="small"
                  @click="contactMerchant(order)"
                >
                  <el-icon><ChatLineRound /></el-icon> 联系商家
                </el-button>
              </div>
            </div>
          </el-card>
        </el-scrollbar>
      </div>
      
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, ChatLineRound } from '@element-plus/icons-vue'
import { 
  getUserOrders, 
  cancelOrder as cancelOrderApi,
  confirmReceived as confirmReceivedApi,
  applyAfterSale as applyAfterSaleApi,
} from '../../api/order'
import { useChatStore } from '../../stores/chat'
import ChatWindow from '../../components/chat/ChatWindow.vue'

const router = useRouter()
const loading = ref(true)
const orders = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 聊天相关状态
const chatStore = useChatStore()
const chatDialogVisible = ref(false)
const activeConversationId = ref<number | null>(null)

// 获取订单列表
const fetchOrders = async (page = 1) => {
  loading.value = true
  try {
    const response = await getUserOrders(page, pageSize.value)
    console.log('获取订单响应:', response)
    
    // 处理不同的响应结构
    let ordersData = [];
    
    if (response && response.data && response.data.success) {
      // 处理新格式：{success: true, data: [...]}
      const actualPayload = response.data.data;
      
      if (Array.isArray(actualPayload)) {
        ordersData = actualPayload;
        total.value = actualPayload.length;
      } else if (actualPayload && typeof actualPayload === 'object') {
        if (actualPayload.items !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.items;
          total.value = actualPayload.total;
        } else if (actualPayload.content !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.content;
          total.value = actualPayload.total;
        } else if (actualPayload.records !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.records;
          total.value = actualPayload.total;
        } else {
          console.error('未知的响应数据结构 (actualPayload):', actualPayload);
        }
      } else {
        console.error('未知的响应数据类型 (actualPayload):', actualPayload);
      }
    } else if (response && response.records) {
      // 后端直接返回分页对象的情况
      ordersData = response.records
      total.value = response.total
    } else if (response && response.data && response.data.records) {
      // 后端返回嵌套在data字段的分页对象的情况
      ordersData = response.data.records
      total.value = response.data.total
    } else if (response && response.data && response.data.items) {
      // 后端返回嵌套在data字段的items对象的情况
      ordersData = response.data.items
      total.value = response.data.total
    } else if (response && response.data && response.data.content) {
      // 后端返回嵌套在data字段的content对象的情况
      ordersData = response.data.content
      total.value = response.data.total
    } else if (Array.isArray(response)) {
      // 后端直接返回数组的情况
      ordersData = response
      total.value = response.length
    } else if (response && response.data && Array.isArray(response.data)) {
      // 后端返回嵌套在data字段的数组的情况
      ordersData = response.data
      total.value = response.data.length
    } else if (response && response.content) {
      // 后端直接返回content字段的情况
      ordersData = response.content
      total.value = response.total || 0
    } else if (response && response.items) {
      // 后端直接返回items字段的情况
      ordersData = response.items
      total.value = response.total || 0
    } else {
      // 未知响应结构，设置为空数组
      console.error('未知的响应数据格式:', response)
      ordersData = []
      total.value = 0
    }
    
    // 确保订单状态逻辑正确
    ordersData.forEach(order => {
      // 如果订单有退款支付记录，确保显示为已退款
      if (order.payment && order.payment.status === 'refunded') {
        order.status = 'refunded';
      }
    });
    
    orders.value = ordersData;
    console.log('处理后的订单列表:', orders.value);
  } catch (error) {
    console.error('获取订单列表失败', error)
    ElMessage.error('获取订单列表失败')
    // 出错时确保orders是空数组而非undefined
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchOrders(page)
}

// 前往商品页面
const goToProducts = () => {
  router.push('/shop/products')
}

// 前往支付页面
const goToPayment = (orderId: number) => {
  router.push(`/shop/payment/${orderId}`)
}

// 查看订单详情
const viewOrderDetail = (orderId: number) => {
  router.push(`/user/orders/${orderId}`)
}

// 添加订单详情弹窗功能
const viewOrderDetailWithAfterSale = (order: any) => {
  // 同步订单状态显示
  let orderStatus = order.status;
  
  // 如果订单有退款支付记录，状态应该是已退款
  if (order.payment && order.payment.status === 'refunded') {
    orderStatus = 'refunded';
  }
  
  let detailContent = `
    <div style="margin-bottom: 15px;">
      <h3 style="margin-bottom: 10px;">订单信息</h3>
      <p><strong>订单号:</strong> ${order.orderNumber}</p>
      <p><strong>下单时间:</strong> ${formatDate(order.createTime)}</p>
      <p><strong>订单状态:</strong> ${getStatusText(orderStatus)}</p>
      <p><strong>订单金额:</strong> ¥${order.totalAmount}</p>
    </div>
  `;
  
  // 如果有售后信息，显示售后状态
  if (order.afterSaleStatus) {
    const afterSaleStatusText = {
      'pending': '处理中',
      'approved': '已同意',
      'rejected': '已拒绝'
    }[order.afterSaleStatus] || order.afterSaleStatus;
    
    detailContent += `
      <div style="margin-bottom: 15px;">
        <h3 style="margin-bottom: 10px;">售后信息</h3>
        <p><strong>售后状态:</strong> ${afterSaleStatusText}</p>
        <p><strong>售后原因:</strong> ${order.afterSaleReason || '无'}</p>
      </div>
    `;
  }
  
  ElMessageBox.alert(
    detailContent,
    '订单详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      callback: () => {}
    }
  );
}

// 取消订单
const cancelOrder = (orderId: number) => {
  ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelOrderApi(orderId)
      ElMessage.success('订单已取消')
      fetchOrders(currentPage.value)
    } catch (error) {
      console.error('取消订单失败', error)
      ElMessage.error('取消订单失败')
    }
  }).catch(() => {})
}

// 确认收货
const confirmReceived = (orderId: number) => {
  ElMessageBox.confirm('确认已收到商品？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await confirmReceivedApi(orderId)
      ElMessage.success('已确认收货')
      fetchOrders(currentPage.value)
    } catch (error) {
      console.error('确认收货失败', error)
      ElMessage.error('确认收货失败')
    }
  }).catch(() => {})
}

// 申请售后
const applyAfterSale = (orderId: number) => {
  ElMessageBox.prompt('请输入申请售后的原因', '申请售后', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：商品有质量问题',
    inputValidator: (value) => {
      if (!value || value.trim() === '') {
        return '售后原因不能为空';
      }
      return true;
    }
  }).then(async ({ value }) => {
    try {
      await applyAfterSaleApi(orderId, value);
      ElMessage.success('售后申请已提交');
      fetchOrders(currentPage.value);
    } catch (error) {
      console.error('申请售后失败', error);
      ElMessage.error('申请售后失败');
    }
  }).catch(() => {});
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 获取订单状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待支付',
    paid: '已支付',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款',
    after_sale: '售后中',
    after_sale_rejected: '售后拒绝'
  }
  return statusMap[status] || status
}

// 处理图片URL
const formatImageUrl = (url: string | null | undefined) => {
  if (!url) return '/placeholder.png'
  
  // 处理多图片URL情况（逗号分隔）
  if (url.includes(',')) {
    const firstUrl = url.split(',')[0].trim()
    return formatImageUrl(firstUrl)
  }
  
  // 处理相对路径情况
  if (url.startsWith('/uploads/')) {
    // 添加时间戳防止缓存
    return `${url}?t=${new Date().getTime()}`
  }
  
  // 其他情况返回原URL
  return url
}

// 联系商家
const contactMerchant = async (order: any) => {
  // 尝试从订单中获取商家ID
  let merchantId = order.merchantId;
  
  // 如果订单本身没有商家ID，尝试从订单项中获取
  if (!merchantId && order.items && order.items.length > 0) {
    const firstItem = order.items[0];
    if (firstItem.merchantId) {
      merchantId = firstItem.merchantId;
    }
  }
  
  if (!merchantId) {
    ElMessage.warning('该订单无法获取商家信息');
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
    
    // 默认发送订单号信息
    const initialMessage = `您好，我想咨询订单 ${order.orderNumber} 的相关问题`;
    if (conversation && conversation.id && initialMessage) {
      chatStore.setInitialMessage(initialMessage);
    }
    
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

// 处理聊天对话框关闭
const handleChatDialogClosed = () => {
  // 清除活跃会话ID
  localStorage.removeItem('activeConversationId');
};

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.order-content {
  margin-bottom: 40px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 确保flex子元素可以正确滚动 */
}

.empty-orders {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.order-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 确保flex子元素可以正确滚动 */
}

.orders-scrollbar {
  height: calc(100vh - 200px); /* 适应屏幕高度减去其他元素高度 */
  overflow-y: auto;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.order-id,
.order-time {
  display: flex;
  align-items: center;
}

.label {
  color: #909399;
  margin-right: 5px;
}

.value {
  color: #303133;
}

.order-status {
  margin-left: auto;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.order-status.pending {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.order-status.paid {
  color: #409eff;
  background-color: #ecf5ff;
}

.order-status.shipped {
  color: #409eff;
  background-color: #ecf5ff;
}

.order-status.completed {
  color: #67c23a;
  background-color: #f0f9eb;
}

.order-status.cancelled {
  color: #909399;
  background-color: #f4f4f5;
}

.order-status.refunded {
  color: #f56c6c;
  background-color: #fef0f0;
}

.order-status.after_sale {
  color: #E6A23C;
  background-color: #FCF6E8;
}

.order-status.after_sale_rejected {
  color: #F56C6C;
  background-color: #FEF0F0;
}
.order-items {
  margin: 15px 0;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  margin-right: 15px;
  border: 1px solid #eee;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.item-price {
  font-size: 14px;
  color: #f56c6c;
}

.item-quantity {
  margin: 0 15px;
  color: #606266;
}

.item-subtotal {
  width: 150px;
  text-align: right;
}

.item-subtotal .amount {
  font-size: 16px;
  color: #f56c6c;
  font-weight: bold;
}

.item-subtotal .merchant {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  font-size: 16px;
}

.order-total .amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.order-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .order-list-container {
    padding: 10px;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .order-status {
    margin-left: 0;
    margin-top: 5px;
  }
  
  .order-item {
    flex-wrap: wrap;
  }
  
  .item-info {
    width: calc(100% - 95px);
  }
  
  .item-quantity {
    margin-left: 95px;
    margin-top: 10px;
  }
  
  .item-subtotal {
    width: auto;
    margin-left: auto;
    margin-top: 10px;
  }
  
  .order-footer {
    flex-direction: column;
    gap: 15px;
  }
  
  .order-actions {
    width: 100%;
    justify-content: flex-end;
  }
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