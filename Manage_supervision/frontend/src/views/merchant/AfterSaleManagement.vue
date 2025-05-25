<template>
  <div class="after-sale-management">
    <div class="page-header">
      <h1>售后管理</h1>
    </div>
    
    <div class="content" v-loading="loading">
      <div v-if="(!orders || orders.length === 0) && !loading" class="empty-content">
        <el-empty description="暂无售后订单"></el-empty>
      </div>
      
      <template v-else>
        <el-table
          :data="orders"
          style="width: 100%"
          border
          stripe
          row-key="id"
          :default-sort="{ prop: 'updateTime', order: 'descending' }"
        >
          <el-table-column prop="orderNumber" label="订单号" min-width="160" show-overflow-tooltip />
          
          <el-table-column prop="username" label="用户" min-width="120" show-overflow-tooltip />
          
          <el-table-column prop="totalAmount" label="金额" min-width="120">
            <template #default="scope">
              ¥{{ scope.row.totalAmount }}
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="订单状态" min-width="120">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="afterSaleStatus" label="售后状态" min-width="120">
            <template #default="scope">
              <el-tag :type="getAfterSaleStatusTagType(scope.row.afterSaleStatus)">
                {{ getAfterSaleStatusText(scope.row.afterSaleStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="afterSaleReason" label="售后原因" min-width="200" show-overflow-tooltip />
          
          <el-table-column prop="createTime" label="创建时间" min-width="160" sortable>
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="updateTime" label="更新时间" min-width="160" sortable>
            <template #default="scope">
              {{ formatDate(scope.row.updateTime) }}
            </template>
          </el-table-column>
          
          <el-table-column fixed="right" label="操作" min-width="200">
            <template #default="scope">
              <div class="action-buttons">
                <el-button
                  v-if="scope.row.afterSaleStatus === 'pending'"
                  type="success"
                  size="small"
                  @click="handleAfterSale(scope.row, true)"
                >同意退款</el-button>
                
                <el-button
                  v-if="scope.row.afterSaleStatus === 'pending'"
                  type="danger"
                  size="small"
                  @click="handleAfterSale(scope.row, false)"
                >拒绝申请</el-button>
                
                <el-button
                  type="primary"
                  plain
                  size="small"
                  @click="viewOrderDetail(scope.row)"
                >订单详情</el-button>
                
                <el-button
                  type="info"
                  plain
                  size="small"
                  @click="contactUser(scope.row)"
                >联系买家</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </div>
    
    <!-- 用户聊天对话框 -->
    <el-dialog
      v-model="chatDialogVisible"
      title="联系买家"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useChatStore } from '../../stores/chat'
import ChatWindow from '../../components/chat/ChatWindow.vue'
import { 
  getMerchantAfterSaleOrders,
  processAfterSale as processAfterSaleApi
} from '../../api/order'

const loading = ref(true)
const orders = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 聊天相关状态
const chatStore = useChatStore()
const chatDialogVisible = ref(false)
const activeConversationId = ref<number | null>(null)

// 获取售后订单列表
const fetchOrders = async (page = 1) => {
  loading.value = true
  try {
    const response = await getMerchantAfterSaleOrders(page, pageSize.value)
    console.log('获取售后订单响应:', response)
    
    // 处理不同的响应结构
    let ordersData = [];
    
    if (response && response.data && response.data.success) {
      // 处理新格式：{success: true, data: [...]}
      const actualPayload = response.data.data;
      
      if (Array.isArray(actualPayload)) {
        ordersData = actualPayload;
        total.value = actualPayload.length;
      } else if (actualPayload && typeof actualPayload === 'object') {
        if (actualPayload.records !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.records;
          total.value = actualPayload.total;
        } else if (actualPayload.content !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.content;
          total.value = actualPayload.total;
        } else if (actualPayload.items !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.items;
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
    } else {
      // 未知响应结构，设置为空数组
      console.error('未知的响应数据格式:', response)
      ordersData = []
      total.value = 0
    }
    
    orders.value = ordersData;
    console.log('处理后的售后订单列表:', orders.value);
  } catch (error) {
    console.error('获取售后订单列表失败', error)
    ElMessage.error('获取售后订单列表失败')
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

// 获取订单状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    paid: 'primary',
    shipped: 'primary',
    completed: 'success',
    cancelled: 'info',
    refunded: 'danger',
    after_sale: 'warning',
    after_sale_rejected: 'danger'
  }
  return typeMap[status] || ''
}

// 获取售后状态文本
const getAfterSaleStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '处理中',
    approved: '已同意',
    rejected: '已拒绝'
  }
  return statusMap[status] || status
}

// 获取售后状态标签类型
const getAfterSaleStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || ''
}

// 处理售后申请
const handleAfterSale = (order: any, approved: boolean) => {
  const action = approved ? '同意' : '拒绝';
  
  ElMessageBox.prompt(`请输入${action}售后的原因`, `${action}售后申请`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: `请输入${action}售后的原因`,
    inputValidator: (value) => {
      if (!value || value.trim() === '') {
        return '原因不能为空';
      }
      return true;
    }
  }).then(async ({ value }) => {
    try {
      await processAfterSaleApi(order.id, approved, value);
      ElMessage.success(`已${action}售后申请`);
      fetchOrders(currentPage.value);
    } catch (error) {
      console.error(`${action}售后申请失败`, error);
      ElMessage.error(`${action}售后申请失败`);
    }
  }).catch(() => {});
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 查看订单详情
const viewOrderDetail = (order: any) => {
  let detailContent = `
    <div style="margin-bottom: 15px;">
      <h3 style="margin-bottom: 10px;">订单信息</h3>
      <p><strong>订单号:</strong> ${order.orderNumber}</p>
      <p><strong>用户:</strong> ${order.username}</p>
      <p><strong>下单时间:</strong> ${formatDate(order.createTime)}</p>
      <p><strong>订单状态:</strong> ${getStatusText(order.status)}</p>
      <p><strong>订单金额:</strong> ¥${order.totalAmount}</p>
      <p><strong>收货地址:</strong> ${order.address || '无'}</p>
      <p><strong>联系电话:</strong> ${order.phone || '无'}</p>
      <p><strong>收货人:</strong> ${order.recipientName || '无'}</p>
    </div>
  `;
  
  // 售后信息
  detailContent += `
    <div style="margin-bottom: 15px;">
      <h3 style="margin-bottom: 10px;">售后信息</h3>
      <p><strong>售后状态:</strong> ${getAfterSaleStatusText(order.afterSaleStatus)}</p>
      <p><strong>售后原因:</strong> ${order.afterSaleReason || '无'}</p>
    </div>
  `;
  
  // 订单商品信息
  if (order.items && order.items.length > 0) {
    detailContent += `
      <div style="margin-bottom: 15px;">
        <h3 style="margin-bottom: 10px;">订单商品</h3>
        <ul style="padding-left: 20px;">
    `;
    
    order.items.forEach((item: any) => {
      detailContent += `
        <li style="margin-bottom: 10px;">
          <p><strong>${item.productName}</strong> x${item.quantity}</p>
          <p>单价: ¥${item.productPrice} | 小计: ¥${item.subtotal}</p>
        </li>
      `;
    });
    
    detailContent += `
        </ul>
      </div>
    `;
  }
  
  // 备注信息
  if (order.remark) {
    detailContent += `
      <div style="margin-bottom: 15px;">
        <h3 style="margin-bottom: 10px;">备注</h3>
        <p>${order.remark.replace(/\n/g, '<br>')}</p>
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

// 联系用户
const contactUser = async (order: any) => {
  if (!order.userId) {
    ElMessage.warning('无法获取用户信息');
    return;
  }

  try {
    // 初始化聊天服务
    if (!chatStore.initialized) {
      await chatStore.initChat();
    }

    // 获取或创建与用户的会话
    const conversation = await chatStore.getOrCreateConversationWithUser(order.userId);
    
    // 设置当前会话ID
    activeConversationId.value = conversation.id;
    // 同时存储到localStorage，用于通知服务判断
    localStorage.setItem('activeConversationId', conversation.id.toString());
    
    // 默认发送订单号信息
    const initialMessage = `您好，关于您的订单 ${order.orderNumber} 的售后申请，我需要与您沟通一下`;
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
.after-sale-management {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.content {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.empty-content {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 聊天对话框样式 */
.chat-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.chat-container {
  height: 70vh;
  min-height: 400px;
}

@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons button {
    width: 100%;
    margin-left: 0 !important;
    margin-right: 0 !important;
    margin-bottom: 8px;
  }
}
</style> 