<template>
  <div class="payment-container">
    <div class="page-header">
      <el-page-header @back="goBack" title="订单支付" />
    </div>
    
    <div class="payment-content" v-loading="loading">
      <el-card shadow="never" class="payment-card" v-if="order">
        <template #header>
          <div class="payment-header">
            <h3>订单信息</h3>
            <div class="order-status" :class="order.status">
              {{ getStatusText(order.status) }}
            </div>
          </div>
        </template>
        
        <div class="order-info">
          <div class="info-item">
            <span class="label">订单号:</span>
            <span class="value">{{ order.orderNumber }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDate(order.createTime) }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">收货人:</span>
            <span class="value">{{ order.recipientName }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">联系电话:</span>
            <span class="value">{{ order.phone }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">收货地址:</span>
            <span class="value">{{ order.address }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">备注:</span>
            <span class="value">{{ order.remark || '无' }}</span>
          </div>
        </div>
        
        <div class="order-amount">
          <span class="label">支付金额:</span>
          <span class="amount">¥{{ order.totalAmount }}</span>
        </div>
      </el-card>
      
      <el-card v-if="order && order.status === 'pending'" shadow="never" class="payment-method-card">
        <template #header>
          <div class="card-header">
            <h3>选择支付方式</h3>
          </div>
        </template>
        
        <div class="payment-methods">
          <div
            v-for="method in paymentMethods"
            :key="method.value"
            class="payment-method-item"
            :class="{ active: selectedMethod === method.value }"
            @click="selectedMethod = method.value"
          >
            <div class="method-icon">
              <el-icon>
                <component :is="method.icon"></component>
              </el-icon>
            </div>
            <div class="method-name">{{ method.label }}</div>
          </div>
        </div>
        
        <div class="payment-actions">
          <el-button
            type="primary"
            size="large"
            @click="createPaymentAndPay"
            :loading="paying"
            :disabled="!selectedMethod"
          >
            立即支付
          </el-button>
          
          <el-button
            type="danger"
            plain
            size="large"
            @click="cancelOrder"
            :loading="cancelling"
          >
            取消订单
          </el-button>
        </div>
      </el-card>
      
      <el-card v-if="order && order.status !== 'pending' && order.payment" shadow="never" class="payment-info-card">
        <template #header>
          <div class="card-header">
            <h3>支付信息</h3>
          </div>
        </template>
        
        <div class="payment-info">
          <div class="info-item">
            <span class="label">支付单号:</span>
            <span class="value">{{ order.payment.paymentNumber }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">支付方式:</span>
            <span class="value">{{ getPaymentMethodText(order.payment.paymentMethod) }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">支付金额:</span>
            <span class="value amount">¥{{ order.payment.amount }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">支付状态:</span>
            <span class="value" :class="getSynchronizedPaymentStatus(order)">
              {{ getPaymentStatusText(getSynchronizedPaymentStatus(order)) }}
            </span>
          </div>
          
          <div class="info-item" v-if="order.payment.createTime">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDate(order.payment.createTime) }}</span>
          </div>
          
          <div class="info-item" v-if="order.payment.updateTime && getSynchronizedPaymentStatus(order) !== 'pending'">
            <span class="label">{{ getSynchronizedPaymentStatus(order) === 'completed' ? '支付时间:' : '更新时间:' }}</span>
            <span class="value">{{ formatDate(order.payment.updateTime) }}</span>
          </div>
          
          <div class="info-item" v-if="order.payment.transactionId">
            <span class="label">交易号:</span>
            <span class="value">{{ order.payment.transactionId }}</span>
          </div>
          
          <div class="payment-actions" v-if="order.status === 'paid'">
            <el-button type="primary" @click="goToOrders">查看我的订单</el-button>
          </div>
        </div>
      </el-card>
      
      <!-- 支付模拟框 -->
      <el-dialog
        v-model="paymentDialogVisible"
        title="支付确认"
        width="360px"
        center
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
      >
        <div class="payment-dialog-content">
          <div class="payment-qrcode" v-if="selectedMethod === 'alipay' || selectedMethod === 'wechat'">
            <div class="qrcode-placeholder">
              <el-icon class="qrcode-icon"><Promotion /></el-icon>
              <span>模拟支付二维码</span>
            </div>
          </div>
          
          <div class="credit-card-form" v-else-if="selectedMethod === 'credit_card'">
            <el-form ref="creditCardFormEl" :model="creditCardForm" label-position="top">
              <el-form-item label="卡号">
                <el-input v-model="creditCardForm.cardNumber" placeholder="输入卡号" @input="updateCreditCardForm" />
              </el-form-item>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="有效期">
                    <el-input v-model="creditCardForm.expiry" placeholder="MM/YY" @input="updateCreditCardForm" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="安全码">
                    <el-input v-model="creditCardForm.cvv" placeholder="CVV" @input="updateCreditCardForm" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
          
          <div class="payment-amount">
            <div class="label">支付金额</div>
            <div class="amount">¥{{ currentPayment?.amount || order?.totalAmount }}</div>
          </div>
          
          <div class="payment-countdown">
            <span>支付倒计时: {{ paymentCountdown }}秒</span>
          </div>
        </div>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="cancelPayment" :disabled="paymentProcessing">取消支付</el-button>
            <el-button type="primary" @click="confirmPayment" :loading="paymentProcessing">
              确认支付
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion, WalletFilled, CreditCard, Money } from '@element-plus/icons-vue'
import { 
  getOrderDetail, 
  cancelOrder as cancelOrderApi,
  getMerchantOrderDetail
} from '../../api/order'
import { createPayment, completePayment, cancelPayment as cancelPaymentApi } from '../../api/payment'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const order = ref<any>(null)
const selectedMethod = ref('')
const paying = ref(false)
const cancelling = ref(false)
const paymentDialogVisible = ref(false)
const paymentProcessing = ref(false)
const paymentCountdown = ref(120) // 2分钟支付倒计时
const countdownTimer = ref<any>(null)
const currentPayment = ref<any>(null)

// 信用卡表单相关
const creditCardFormEl = ref(null)

// 信用卡表单 - 修改为响应式对象
const creditCardForm = reactive({
  cardNumber: '',
  expiry: '',
  cvv: ''
})

// 更新信用卡表单方法
const updateCreditCardForm = () => {
  console.log('信用卡表单已更新:', creditCardForm)
}

// 支付方式列表
const paymentMethods = [
  { label: '支付宝', value: 'alipay', icon: 'Money' },
  { label: '微信支付', value: 'wechat', icon: 'WalletFilled' },
  { label: '信用卡', value: 'credit_card', icon: 'CreditCard' }
]

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    // 添加更多调试输出
    console.log('获取订单详情，路由参数:', route.params)
    console.log('当前用户角色:', userStore.isAdmin ? '管理员' : userStore.isMerchant ? '商家' : '普通用户')
    
    const orderId = parseInt(route.params.id as string)
    console.log('解析后的订单ID:', orderId)
    
    if (isNaN(orderId) || orderId <= 0) {
      console.error('无效的订单ID:', route.params.id)
      ElMessage.error('无效的订单ID')
      redirectBack()
      return
    }
    
    console.log('开始请求订单详情, ID:', orderId)
    // 根据用户角色选择正确的API
    let response
    if (userStore.isMerchant) {
      console.log('使用商家订单详情API')
      response = await getMerchantOrderDetail(orderId)
    } else {
      console.log('使用普通用户订单详情API')
      response = await getOrderDetail(orderId)
    }
    
    console.log('获取到订单详情响应:', response)
    
    if (!response) {
      console.error('订单详情响应为空')
      ElMessage.error('获取订单详情失败，响应为空')
      redirectBack()
      return
    }
    
    // 处理嵌套的响应结构
    let orderData;
    if (response.success === true && response.data) {
      console.log('从响应的data字段提取订单数据:', response.data)
      orderData = response.data
    } else {
      // 如果没有嵌套结构，直接使用响应
      orderData = response
    }
    
    // 处理订单状态
    if (orderData.payment && orderData.payment.status === 'refunded') {
      orderData.status = 'refunded';
    }
    
    order.value = orderData;
    
    console.log('设置订单数据:', order.value)
    
    // 如果订单已支付，获取支付信息
    if (order.value && order.value.status) {
      console.log('订单状态:', order.value.status)
      if (order.value.status !== 'pending') {
        console.log('订单状态非待支付:', order.value.status)
      }
    } else {
      console.error('订单数据格式异常，无法读取状态:', order.value)
    }
  } catch (error) {
    console.error('获取订单详情失败', error)
    ElMessage.error('获取订单详情失败')
    redirectBack()
  } finally {
    loading.value = false
  }
}

// 根据用户角色决定返回的页面
const redirectBack = () => {
  if (userStore.isMerchant) {
    router.push('/merchant/orders')
  } else {
    router.push('/user/orders')
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 前往我的订单页面
const goToOrders = () => {
  if (userStore.isMerchant) {
    router.push('/merchant/orders')
  } else {
    router.push('/user/orders')
  }
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
    refunded: '已退款'
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
    failed: '支付失败',
    refund_pending: '退款处理中'
  }
  return statusMap[status] || status
}

// 同步订单状态和支付状态
const getSynchronizedPaymentStatus = (orderData: any) => {
  // 如果订单已支付，但支付状态仍为待支付，则返回已支付状态
  if (orderData.status === 'paid' && orderData.payment && orderData.payment.status === 'pending') {
    return 'completed'
  }
  
  // 如果订单已取消，但支付状态不是取消，则返回取消状态
  if (orderData.status === 'cancelled' && orderData.payment && orderData.payment.status !== 'cancelled') {
    return 'cancelled'
  }
  
  // 如果订单已退款，但支付状态不是退款，则返回退款状态
  if (orderData.status === 'refunded' && orderData.payment && orderData.payment.status !== 'refunded') {
    return 'refunded'
  }
  
  // 其他情况返回原始支付状态
  return orderData.payment ? orderData.payment.status : ''
}

// 创建支付并进入支付流程
const createPaymentAndPay = async () => {
  if (!selectedMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  paying.value = true
  
  try {
    const paymentData = {
      orderId: order.value.id,
      paymentMethod: selectedMethod.value,
      amount: order.value.totalAmount
    }
    
    // 创建支付
    const response = await createPayment(paymentData)
    currentPayment.value = response
    
    // 弹出支付确认框
    paymentDialogVisible.value = true
    
    // 开始倒计时
    startPaymentCountdown()
  } catch (error) {
    console.error('创建支付失败', error)
    ElMessage.error('创建支付失败，请重试')
  } finally {
    paying.value = false
  }
}

// 开始支付倒计时
const startPaymentCountdown = () => {
  // 重置倒计时
  paymentCountdown.value = 120
  
  // 清除旧的定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  // 设置新的定时器
  countdownTimer.value = setInterval(() => {
    if (paymentCountdown.value > 0) {
      paymentCountdown.value--
    } else {
      // 倒计时结束，自动取消支付
      clearInterval(countdownTimer.value)
      if (paymentDialogVisible.value) {
        ElMessage.warning('支付超时，请重新支付')
        paymentDialogVisible.value = false
        
        // 取消支付
        if (currentPayment.value) {
          // 获取支付ID，支持多种可能的数据结构
          const paymentId = currentPayment.value.id || 
                           (currentPayment.value.data && currentPayment.value.data.id) ||
                           (typeof currentPayment.value === 'number' ? currentPayment.value : null)
                           
          if (paymentId) {
            console.log('支付超时自动取消，支付ID:', paymentId)
            cancelPaymentApi(paymentId).catch(error => {
              console.error('取消支付失败', error)
            })
          } else {
            console.error('支付超时取消失败：无效的支付ID', currentPayment.value)
          }
        }
      }
    }
  }, 1000)
}

// 确认支付
const confirmPayment = async () => {
  if (!currentPayment.value) {
    ElMessage.error('支付信息错误，请重新支付')
    paymentDialogVisible.value = false
    return
  }
  
  paymentProcessing.value = true
  
  try {
    // 模拟支付过程
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 获取支付ID，支持多种可能的数据结构
    const paymentId = currentPayment.value.id || 
                     (currentPayment.value.data && currentPayment.value.data.id) ||
                     (typeof currentPayment.value === 'number' ? currentPayment.value : null)
    
    // 确保paymentId存在且有效
    if (!paymentId) {
      console.error('支付ID无效:', currentPayment.value)
      ElMessage.error('支付信息无效，无法完成支付')
      paymentDialogVisible.value = false
      paymentProcessing.value = false
      return
    }
    
    console.log('确认支付ID:', paymentId)
    
    // 完成支付
    await completePayment(paymentId)
    
    // 清除倒计时
    if (countdownTimer.value) {
      clearInterval(countdownTimer.value)
    }
    
    paymentDialogVisible.value = false
    ElMessage.success('支付成功')
    
    // 刷新订单信息
    fetchOrderDetail()
  } catch (error) {
    console.error('支付失败', error)
    ElMessage.error('支付失败，请重试')
  } finally {
    paymentProcessing.value = false
  }
}

// 取消支付
const cancelPayment = async () => {
  if (!currentPayment.value) {
    paymentDialogVisible.value = false
    return
  }
  
  try {
    // 先打印完整的支付对象，便于调试
    console.log('当前支付对象:', currentPayment.value)
    
    // 获取支付ID，支持多种可能的数据结构
    const paymentId = currentPayment.value.id || 
                     (currentPayment.value.data && currentPayment.value.data.id) ||
                     (typeof currentPayment.value === 'number' ? currentPayment.value : null)
    
    // 确保paymentId存在且有效
    if (!paymentId) {
      console.error('支付ID无效:', currentPayment.value)
      ElMessage.error('支付信息无效，无法取消')
      paymentDialogVisible.value = false
      return
    }
    
    console.log('取消支付ID:', paymentId)
    
    // 取消支付
    await cancelPaymentApi(paymentId)
    
    // 清除倒计时
    if (countdownTimer.value) {
      clearInterval(countdownTimer.value)
    }
    
    paymentDialogVisible.value = false
    ElMessage.info('支付已取消')
    
    // 重置当前支付信息
    currentPayment.value = null
  } catch (error) {
    console.error('取消支付失败', error)
    ElMessage.error('取消支付失败')
  }
}

// 取消订单
const cancelOrder = async () => {
  ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    cancelling.value = true
    
    try {
      await cancelOrderApi(order.value.id)
      ElMessage.success('订单已取消')
      
      // 刷新订单信息
      fetchOrderDetail()
    } catch (error) {
      console.error('取消订单失败', error)
      ElMessage.error('取消订单失败')
    } finally {
      cancelling.value = false
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchOrderDetail()
})

onBeforeUnmount(() => {
  // 清除定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
})
</script>

<style scoped>
.payment-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.payment-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.order-status {
  font-size: 14px;
  padding: 4px 10px;
  border-radius: 4px;
  background-color: #f0f0f0;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.order-info {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
}

.info-item .label {
  width: 100px;
  color: #909399;
  flex-shrink: 0;
}

.info-item .value {
  color: #303133;
  word-break: break-all;
}

.info-item .value.completed {
  color: #67c23a;
}

.info-item .value.pending {
  color: #e6a23c;
}

.info-item .value.cancelled {
  color: #909399;
}

.info-item .value.refunded {
  color: #f56c6c;
}

.order-amount {
  display: flex;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.order-amount .label {
  color: #303133;
  font-weight: bold;
  font-size: 16px;
  margin-right: 10px;
}

.order-amount .amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 24px;
}

.payment-methods {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.payment-method-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 100px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-method-item:hover {
  border-color: #409eff;
}

.payment-method-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.method-icon {
  font-size: 30px;
  margin-bottom: 10px;
  color: #606266;
}

.payment-method-item.active .method-icon {
  color: #409eff;
}

.method-name {
  font-size: 14px;
  color: #606266;
}

.payment-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.payment-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.payment-qrcode {
  width: 200px;
  height: 200px;
  margin-bottom: 20px;
}

.qrcode-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.qrcode-icon {
  font-size: 50px;
  color: #409eff;
  margin-bottom: 10px;
}

.credit-card-form {
  width: 100%;
  margin-bottom: 20px;
}

.payment-amount {
  text-align: center;
  margin-bottom: 15px;
}

.payment-amount .label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.payment-amount .amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.payment-countdown {
  margin-top: 10px;
  color: #f56c6c;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
}

@media (max-width: 768px) {
  .payment-container {
    padding: 10px;
  }
  
  .payment-methods {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .info-item .label {
    width: 80px;
  }
}
</style> 