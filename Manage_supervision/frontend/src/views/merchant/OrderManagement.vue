<template>
  <div class="order-management-container">
    <div class="page-header">
      <h1>订单管理</h1>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="订单状态" clearable @change="handleFilterChange">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
    </div>
    
    <div class="order-content" v-loading="loading">
      <div v-if="(!orders || orders.length === 0) && !loading" class="empty-orders">
        <el-empty description="暂无订单" />
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
                
                <div class="order-customer">
                  <span class="label">客户:</span>
                  <span class="value">{{ order.username }}</span>
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
              <div class="section-title">订单商品</div>
              <div class="items-table">
                <el-table :data="filterMerchantItems(order.items)" style="width: 100%">
                  <el-table-column prop="productName" label="商品名称">
                    <template #default="scope">
                      <div class="product-info">
                        <el-image
                          class="product-image"
                          :src="formatImageUrl(scope.row.productImageUrl)"
                          fit="cover"
                        >
                          <template #error>
                            <div class="image-placeholder">
                              <el-icon><Picture /></el-icon>
                            </div>
                          </template>
                        </el-image>
                        <span>{{ scope.row.productName }}</span>
                      </div>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="productPrice" label="单价" width="100">
                    <template #default="scope">
                      ¥{{ scope.row.productPrice }}
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="quantity" label="数量" width="80" />
                  
                  <el-table-column prop="subtotal" label="小计" width="120">
                    <template #default="scope">
                      ¥{{ scope.row.subtotal }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            
            <div class="order-details">
              <div class="section-title">收货信息</div>
              <div class="detail-row">
                <div class="detail-item">
                  <span class="label">收货人:</span>
                  <span class="value">{{ order.recipientName }}</span>
                </div>
                
                <div class="detail-item">
                  <span class="label">联系电话:</span>
                  <span class="value">{{ order.phone }}</span>
                </div>
              </div>
              
              <div class="detail-row">
                <div class="detail-item full-width">
                  <span class="label">收货地址:</span>
                  <span class="value">{{ order.address }}</span>
                </div>
              </div>
              
              <div class="detail-row" v-if="order.remark">
                <div class="detail-item full-width">
                  <span class="label">备注:</span>
                  <span class="value">{{ order.remark }}</span>
                </div>
              </div>
            </div>
            
            <div class="order-payment" v-if="order.payment">
              <div class="section-title">支付信息</div>
              <div class="detail-row">
                <div class="detail-item">
                  <span class="label">支付方式:</span>
                  <span class="value">{{ getPaymentMethodText(order.payment.paymentMethod) }}</span>
                </div>
                
                <div class="detail-item">
                  <span class="label">支付状态:</span>
                  <span class="value" :class="order.payment.status">
                    {{ getPaymentStatusText(order.payment.status) }}
                  </span>
                </div>
                
                <div class="detail-item">
                  <span class="label">支付时间:</span>
                  <span class="value">{{ formatDate(order.payment.updateTime) }}</span>
                </div>
              </div>
            </div>
            
            <div class="order-footer">
              <div class="order-total">
                <span class="label">商家商品金额:</span>
                <span class="amount">¥{{ getMerchantItemsTotal(order.items) }}</span>
              </div>
              
              <div class="order-actions">
                <el-button
                  v-if="order.status === 'paid'"
                  type="primary"
                  size="small"
                  @click="shipOrder(order.id)"
                >
                  发货
                </el-button>
                
                <el-button
                  v-if="order.status === 'paid' || order.status === 'shipped'"
                  type="warning"
                  size="small"
                  @click="refundOrder(order.id)"
                >
                  退款
                </el-button>
                
                <el-button
                  v-if="order.status === 'pending'"
                  type="danger"
                  plain
                  size="small"
                  @click="cancelOrder(order.id)"
                >
                  取消订单
                </el-button>
                
                <el-button
                  type="primary"
                  plain
                  size="small"
                  @click="viewOrderDetailWithAfterSale(order)"
                >
                  详情
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { 
  getMerchantOrders, 
  shipOrder as shipOrderApi,
  refundOrder as refundOrderApi,
  merchantCancelOrder as cancelOrderApi
} from '../../api/order'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const orders = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

// 订单状态选项
const statusOptions = [
  { label: '待支付', value: 'pending' },
  { label: '已支付', value: 'paid' },
  { label: '已发货', value: 'shipped' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
  { label: '已退款', value: 'refunded' },
  { label: '售后中', value: 'after_sale' },
  { label: '售后拒绝', value: 'after_sale_rejected' }
]

// 获取订单列表
const fetchOrders = async (page = 1) => {
  loading.value = true
  try {
    const response = await getMerchantOrders(page, pageSize.value)
    console.log('获取商家订单响应:', response)
    
    // 处理不同的响应结构
    let ordersData = [];
    
    if (response.data && response.data.success) {
      // 处理新格式 {success: true, data: ...}
      const actualPayload = response.data.data;
      
      if (Array.isArray(actualPayload)) {
        ordersData = actualPayload;
        total.value = actualPayload.length;
      } else if (actualPayload && typeof actualPayload === 'object') {
        if (actualPayload.records !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.records;
          total.value = actualPayload.total;
        } else if (actualPayload.items !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.items;
          total.value = actualPayload.total;
        } else if (actualPayload.content !== undefined && actualPayload.total !== undefined) {
          ordersData = actualPayload.content;
          total.value = actualPayload.total;
        } else {
          console.error('未知的响应数据结构 (actualPayload):', actualPayload);
          ordersData = [];
          total.value = 0;
        }
      } else {
        console.error('未知的响应数据类型 (actualPayload):', actualPayload);
        ordersData = [];
        total.value = 0;
      }
    } else {
      // 处理旧格式
    if (response && response.records) {
      // 后端直接返回分页对象的情况
        ordersData = response.records;
        total.value = response.total;
    } else if (response && response.data && response.data.records) {
      // 后端返回嵌套在data字段的分页对象的情况
        ordersData = response.data.records;
        total.value = response.data.total;
    } else if (Array.isArray(response)) {
      // 后端直接返回数组的情况
        ordersData = response;
        total.value = response.length;
    } else if (response && response.data && Array.isArray(response.data)) {
      // 后端返回嵌套在data字段的数组的情况
        ordersData = response.data;
        total.value = response.data.length;
    } else {
      // 未知响应结构，设置为空数组
        console.error('未知的订单响应格式:', response);
        ordersData = [];
        total.value = 0;
      }
    }
    
    // 确保订单状态逻辑正确
    ordersData.forEach(order => {
      // 如果订单有退款支付记录，确保显示为已退款
      if (order.payment && order.payment.status === 'refunded') {
        order.status = 'refunded';
      }
    });
    
    orders.value = ordersData;
  } catch (error) {
    console.error('获取订单列表失败', error);
    ElMessage.error('获取订单列表失败');
    // 出错时确保orders是空数组而非undefined
    orders.value = [];
  } finally {
    loading.value = false;
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchOrders(page)
}

// 筛选变化
const handleFilterChange = () => {
  // 实际项目中这里可以添加后端筛选
  // 这里简单前端筛选
  fetchOrders(1)
}

// 查看订单详情
const viewOrderDetail = (orderId: number) => {
  router.push(`/merchant/orders/${orderId}`)
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
      <p><strong>客户:</strong> ${order.username}</p>
      <p><strong>下单时间:</strong> ${formatDate(order.createTime)}</p>
      <p><strong>订单状态:</strong> ${getStatusText(orderStatus)}</p>
      <p><strong>商家商品金额:</strong> ¥${getMerchantItemsTotal(order.items)}</p>
    </div>
  `;
  
  // 显示商家的商品信息
  const merchantItems = filterMerchantItems(order.items || []);
  if (merchantItems.length > 0) {
    detailContent += `
      <div style="margin-bottom: 15px;">
        <h3 style="margin-bottom: 10px;">商品信息</h3>
        <ul style="padding-left: 20px;">
    `;
    
    merchantItems.forEach(item => {
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

// 发货
const shipOrder = (orderId: number) => {
  ElMessageBox.confirm('确认商品已发货？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await shipOrderApi(orderId)
      ElMessage.success('发货成功')
      fetchOrders(currentPage.value)
    } catch (error) {
      console.error('发货失败', error)
      ElMessage.error('发货失败')
    }
  }).catch(() => {})
}

// 退款
const refundOrder = (orderId: number) => {
  ElMessageBox.prompt('请输入退款原因', '退款', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    inputPattern: /^.+$/,
    inputErrorMessage: '退款原因不能为空'
  }).then(async ({ value }) => {
    try {
      await refundOrderApi(orderId, value)
      ElMessage.success('退款成功')
      fetchOrders(currentPage.value)
    } catch (error) {
      console.error('退款失败', error)
      ElMessage.error('退款失败')
    }
  }).catch(() => {})
}

// 取消订单（仅适用于待支付状态的订单）
const cancelOrder = (orderId: number) => {
  ElMessageBox.prompt('请输入取消订单原因', '取消待支付订单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    inputPattern: /^.+$/,
    inputErrorMessage: '取消原因不能为空'
  }).then(async ({ value }) => {
    try {
      await cancelOrderApi(orderId, value)
      ElMessage.success('订单已取消')
      fetchOrders(currentPage.value)
    } catch (error) {
      console.error('取消订单失败', error)
      ElMessage.error('取消订单失败')
    }
  }).catch(() => {})
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

// 获取支付方式文本
const getPaymentMethodText = (method: string) => {
  const methodMap: Record<string, string> = {
    alipay: '支付宝',
    wechat: '微信支付',
    credit_card: '信用卡'
  }
  return methodMap[method] || method
}

// 获取支付状态文本
const getPaymentStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待支付',
    completed: '支付成功',
    cancelled: '已取消',
    refunded: '已退款',
    failed: '支付失败'
  }
  return statusMap[status] || status
}

// 筛选当前商家的商品
const filterMerchantItems = (items: any[]) => {
  if (!items) return []
  return items.filter(item => item.merchantId === userStore.userId)
}

// 计算当前商家商品总额
const getMerchantItemsTotal = (items: any[]) => {
  if (!items) return 0
  return filterMerchantItems(items).reduce((total, item) => {
    return total + Number(item.subtotal || 0)
  }, 0).toFixed(2)
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

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
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
  justify-content: center;
  padding: 50px 0;
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
.order-customer,
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

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 15px 0 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 50px;
  height: 50px;
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
  font-size: 20px;
}

.order-details,
.order-payment {
  margin-top: 20px;
}

.detail-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 10px;
}

.detail-item {
  display: flex;
  min-width: 200px;
}

.detail-item.full-width {
  width: 100%;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
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
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .order-management-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
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
  
  .detail-item {
    min-width: 100%;
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
</style> 