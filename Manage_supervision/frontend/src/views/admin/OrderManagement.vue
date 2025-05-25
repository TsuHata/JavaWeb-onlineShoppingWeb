<template>
  <div class="page-container">
    <div class="page-title">订单管理</div>
    
    <!-- 搜索筛选区 -->
    <el-card class="filter-container card-shadow mb-20">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNumber" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="待支付" value="pending" />
            <el-option label="已支付" value="paid" />
            <el-option label="已发货" value="shipped" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已退款" value="refunded" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 订单列表 -->
    <el-card class="card-shadow">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
      >
        <el-table-column label="订单号" prop="orderNumber" min-width="180" />
        <el-table-column label="用户" min-width="120">
          <template #default="scope">
            {{ scope.row.username || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column label="金额" prop="totalAmount" min-width="100" />
        <el-table-column label="收货信息" min-width="180">
          <template #default="scope">
            {{ scope.row.recipientName }} / {{ scope.row.phone }}
          </template>
        </el-table-column>
        <el-table-column label="收货地址" prop="address" min-width="200" />
        <el-table-column label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="800px"
      destroy-on-close
    >
      <template v-if="currentOrder">
        <div class="order-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号" :span="2">{{ currentOrder.orderNumber }}</el-descriptions-item>
            <el-descriptions-item label="下单用户">{{ currentOrder.username }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusTag(currentOrder.status)">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="收货人">{{ currentOrder.recipientName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(currentOrder.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="订单备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="subtitle">订单商品</div>
          <el-table :data="currentOrder.items" border style="width: 100%">
            <el-table-column label="商品图片" width="80">
              <template #default="scope">
                <el-image 
                  style="width: 50px; height: 50px" 
                  :src="handleImageUrl(scope.row.productImageUrl)" 
                  fit="cover"
                  :preview-src-list="scope.row.productImageUrl ? [handleImageUrl(scope.row.productImageUrl)] : []"
                >
                  <template #error>
                    <div class="image-placeholder">无图片</div>
                  </template>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column label="商品名称" prop="productName" min-width="200" />
            <el-table-column label="商家" prop="merchantName" width="120" />
            <el-table-column label="单价" prop="productPrice" width="100" />
            <el-table-column label="数量" prop="quantity" width="80" />
            <el-table-column label="小计" prop="subtotal" width="100" />
          </el-table>
          
          <div class="order-total">
            <span class="label">订单总金额：</span>
            <span class="value">¥ {{ currentOrder.totalAmount }}</span>
          </div>
          
          <div class="subtitle" v-if="currentOrder.payment">支付信息</div>
          <el-descriptions v-if="currentOrder.payment" :column="2" border>
            <el-descriptions-item label="支付方式">{{ currentOrder.payment.paymentMethod }}</el-descriptions-item>
            <el-descriptions-item label="交易号">{{ currentOrder.payment.transactionId }}</el-descriptions-item>
            <el-descriptions-item label="支付金额">¥ {{ currentOrder.payment.amount }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatDateTime(currentOrder.payment.paymentTime) }}</el-descriptions-item>
            <el-descriptions-item label="支付状态" :span="2">
              <el-tag :type="getPaymentStatusTag(currentOrder.payment?.status)">
                {{ getPaymentStatusText(currentOrder.payment?.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-dialog>
    
    <!-- 操作确认对话框 -->
    <el-dialog
      v-model="actionVisible"
      :title="actionDialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form v-if="['cancel', 'refund'].includes(currentAction)" :model="actionForm" label-width="80px">
        <el-form-item label="原因">
          <el-input v-model="actionForm.reason" type="textarea" rows="3" placeholder="请输入操作原因" />
        </el-form-item>
      </el-form>
      <span v-else>确认要{{ actionDialogTitle }}吗？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="actionVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmOrderAction" :loading="actionLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

// 订单状态映射
const statusMap = {
  pending: { text: '待支付', tag: 'warning' },
  paid: { text: '已支付', tag: 'success' },
  shipped: { text: '已发货', tag: 'primary' },
  completed: { text: '已完成', tag: 'success' },
  cancelled: { text: '已取消', tag: 'info' },
  refunded: { text: '已退款', tag: 'danger' },
  after_sale_rejected: { text: '售后拒绝', tag: 'danger' },
  after_sale_processing: { text: '售后处理中', tag: 'warning' },
  after_sale_approved: { text: '售后通过', tag: 'success' },
  // 添加中文状态支持
  '已完成': { text: '已完成', tag: 'success' },
  '已发货': { text: '已发货', tag: 'primary' },
  '已取消': { text: '已取消', tag: 'info' },
  '已退款': { text: '已退款', tag: 'danger' }
}

// 数据加载和分页
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  orderNumber: '',
  status: ''
})

// 订单详情
const detailVisible = ref(false)
const currentOrder = ref(null)

// 订单操作
const actionVisible = ref(false)
const actionLoading = ref(false)
const currentAction = ref('')
const actionDialogTitle = ref('')
const actionForm = reactive({
  reason: ''
})
const actionOrderId = ref(null)

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      orderNumber: searchForm.orderNumber || undefined,
      status: searchForm.status || undefined
    }

    const response = await axios.get('/api/admin/orders', { params })
    
    if (response.data.success) {
      tableData.value = response.data.data.records
      pagination.total = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取订单详情
const fetchOrderDetail = async (orderId) => {
  try {
    const response = await axios.get(`/api/admin/orders/${orderId}`)
    
    if (response.data.success) {
      currentOrder.value = response.data.data
      detailVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 搜索和重置
const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrders()
}

const resetSearch = () => {
  searchForm.orderNumber = ''
  searchForm.status = ''
  handleSearch()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchOrders()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchOrders()
}

// 查看订单详情
const handleViewDetail = (row) => {
  fetchOrderDetail(row.id)
}

// 订单操作处理
const handleOrderAction = (action, row) => {
  currentAction.value = action
  actionOrderId.value = row.id
  
  // 设置对话框标题
  switch (action) {
    case 'markPaid':
      actionDialogTitle.value = '标记订单为已支付'
      break
    case 'markShipped':
      actionDialogTitle.value = '标记订单为已发货'
      break
    case 'markCompleted':
      actionDialogTitle.value = '标记订单为已完成'
      break
    case 'cancel':
      actionDialogTitle.value = '取消订单'
      break
    case 'refund':
      actionDialogTitle.value = '订单退款'
      break
  }
  
  // 重置表单
  actionForm.reason = ''
  
  // 显示对话框
  actionVisible.value = true
}

// 确认订单操作
const confirmOrderAction = async () => {
  if (!actionOrderId.value) return
  
  actionLoading.value = true
  try {
    let url = ''
    let params = {}
    
    switch (currentAction.value) {
      case 'markPaid':
        url = `/api/admin/orders/${actionOrderId.value}/status`
        params = { status: 'paid' }
        break
      case 'markShipped':
        url = `/api/admin/orders/${actionOrderId.value}/status`
        params = { status: 'shipped' }
        break
      case 'markCompleted':
        url = `/api/admin/orders/${actionOrderId.value}/status`
        params = { status: 'completed' }
        break
      case 'cancel':
        url = `/api/admin/orders/${actionOrderId.value}/cancel`
        if (actionForm.reason) {
          params = { reason: actionForm.reason }
        }
        break
      case 'refund':
        url = `/api/admin/orders/${actionOrderId.value}/status`
        params = { 
          status: 'refunded',
          reason: actionForm.reason || '管理员退款'
        }
        break
    }
    
    const response = await axios.post(url, null, { params })
    
    if (response.data.success) {
      ElMessage.success('操作成功')
      actionVisible.value = false
      fetchOrders() // 刷新订单列表
      
      // 如果当前有订单详情打开，刷新详情
      if (detailVisible.value && currentOrder.value && currentOrder.value.id === actionOrderId.value) {
        fetchOrderDetail(actionOrderId.value)
      }
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

// 辅助函数
const getStatusText = (status) => {
  if (!status) return '-'
  return statusMap[status]?.text || status
}

const getStatusTag = (status) => {
  if (!status) return 'info'
  return statusMap[status]?.tag || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

// 处理图片URL
const handleImageUrl = (url) => {
  if (!url) return '';
  // 如果包含逗号，说明是多个URL，取第一个
  if (url.includes(',')) {
    return url.split(',')[0];
  }
  return url;
}

// 支付状态处理
const getPaymentStatusText = (status) => {
  if (!status) return '未支付';
  
  // 转小写比较
  const lowerStatus = status.toLowerCase();
  if (lowerStatus === 'success' || lowerStatus === '支付成功') {
    return '支付成功';
  } else if (lowerStatus === 'pending' || lowerStatus === '处理中') {
    return '处理中';
  } else if (lowerStatus === 'failed' || lowerStatus === '支付失败') {
    return '支付失败';
  }
  
  return status; // 返回原状态
}

const getPaymentStatusTag = (status) => {
  if (!status) return 'info';
  
  const lowerStatus = status.toLowerCase();
  if (lowerStatus === 'success' || lowerStatus === '支付成功') {
    return 'success';
  } else if (lowerStatus === 'pending' || lowerStatus === '处理中') {
    return 'warning';
  } else if (lowerStatus === 'failed' || lowerStatus === '支付失败') {
    return 'danger';
  }
  
  return 'info';
}

// 初始化
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.order-detail {
  padding: 0 20px;
}

.subtitle {
  font-size: 16px;
  font-weight: 500;
  margin: 20px 0 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.order-total {
  margin-top: 20px;
  text-align: right;
  font-size: 16px;
}

.order-total .label {
  font-weight: bold;
}

.order-total .value {
  color: #f56c6c;
  font-size: 20px;
  font-weight: 600;
  margin-left: 8px;
}

.image-placeholder {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
}

.el-descriptions {
  margin-bottom: 20px;
}
</style> 