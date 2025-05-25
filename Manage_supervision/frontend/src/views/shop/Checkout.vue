<template>
  <div class="checkout-container">
    <div class="page-header">
      <el-page-header @back="goBack" title="结算" />
    </div>
    
    <div class="checkout-content" v-loading="loading">
      <el-card shadow="never" class="address-card">
        <template #header>
          <div class="section-header">
            <h3>收货信息</h3>
          </div>
        </template>
        
        <el-form ref="addressFormEl" :model="addressForm" :rules="addressRules" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="收货人" prop="recipientName">
                <el-input v-model="addressForm.recipientName" placeholder="请输入收货人姓名" @input="updateForm" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="addressForm.phone" placeholder="请输入联系电话" @input="updateForm" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="收货地址" prop="address">
            <el-input v-model="addressForm.address" placeholder="请输入详细地址" @input="updateForm" />
          </el-form-item>
          
          <el-form-item label="备注" prop="remark">
            <el-input v-model="addressForm.remark" type="textarea" placeholder="备注信息（选填）" @input="updateForm" />
          </el-form-item>
        </el-form>
      </el-card>
      
      <el-card shadow="never" class="items-card">
        <template #header>
          <div class="section-header">
            <h3>商品清单</h3>
          </div>
        </template>
        
        <div class="item-list">
          <div class="item" v-for="item in selectedItems" :key="item.id">
            <div class="item-image">
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
            
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-price">¥{{ item.price }}</div>
            </div>
            
            <div class="item-quantity">
              × {{ item.quantity }}
            </div>
            
            <div class="item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
          </div>
        </div>
      </el-card>
      
      <div class="order-summary">
        <div class="summary-item">
          <span>商品总价</span>
          <span class="price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        
        <div class="summary-item">
          <span>运费</span>
          <span>免运费</span>
        </div>
        
        <div class="summary-total">
          <span>应付总额</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </div>
      
      <div class="checkout-actions">
        <el-button type="primary" size="large" @click="submitOrder" :loading="submitting">
          提交订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useCartStore } from '../../stores/cart'
import { createOrder } from '../../api/order'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const submitting = ref(false)
const addressFormEl = ref(null)

// 地址表单
const addressForm = reactive({
  recipientName: '',
  phone: '',
  address: '',
  remark: ''
})

// 表单校验规则
const addressRules = {
  recipientName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 选中的商品
const selectedItems = computed(() => {
  return cartStore.cartItems.filter(item => item.selected)
})

// 商品总价
const totalPrice = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

// 返回上一页
const goBack = () => {
  router.back()
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

// 更新表单方法
const updateForm = () => {
  console.log('表单已更新:', addressForm)
}

// 提交订单
const submitOrder = async () => {
  // 验证选中商品
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 表单验证
  if (!addressFormEl.value) {
    console.error('表单引用不存在')
    return
  }
  
  try {
    const valid = await addressFormEl.value.validate()
    if (!valid) {
      ElMessage.warning('请填写完整的收货信息')
      return
    }
  } catch (error) {
    console.error('表单验证出错:', error)
    ElMessage.warning('请填写完整的收货信息')
    return
  }
  
  submitting.value = true
  
  try {
    // 准备订单数据
    const orderData = {
      items: selectedItems.value.map(item => ({
        productId: item.id,
        quantity: item.quantity
      })),
      address: addressForm.address,
      phone: addressForm.phone,
      recipientName: addressForm.recipientName,
      remark: addressForm.remark
    }
    
    console.log('提交订单数据:', orderData)
    
    // 创建订单
    const response = await createOrder(orderData)
    console.log('创建订单响应:', response)
    
    // 检查响应中是否包含订单ID
    let orderId = null
    
    if (response && response.data && response.data.success && response.data.data && response.data.data.id) {
      // 处理新格式：{data: {success: true, data: {id: ...}}}
      orderId = response.data.data.id
      console.log('从response.data.data.id获取订单ID:', orderId)
    } else if (response && response.success && response.data && response.data.id) {
      // 处理格式：{success: true, data: {id: ...}}
      orderId = response.data.id
      console.log('从response.data.id获取订单ID:', orderId)
    } else if (response && response.id) {
      // 处理直接返回对象的情况
      orderId = response.id
      console.log('从response.id获取订单ID:', orderId)
    } else if (response && typeof response === 'object') {
      // 尝试遍历响应寻找id
      const findId = (obj) => {
        if (!obj || typeof obj !== 'object') return null
        if (obj.id) return obj.id
        for (const key in obj) {
          if (obj[key] && typeof obj[key] === 'object') {
            const found = findId(obj[key])
            if (found) return found
          }
        }
        return null
      }
      
      orderId = findId(response)
      console.log('递归查找获取订单ID:', orderId)
    }
    
    if (!orderId) {
      console.error('订单创建响应无法获取ID:', response)
      throw new Error('无法从响应中获取有效的订单ID')
    }
    
    // 从购物车移除已购买商品
    selectedItems.value.forEach(item => {
      cartStore.removeFromCart(item.id)
    })
    
    ElMessage.success('订单创建成功，请继续支付')
    
    // 跳转到支付页面
    router.push(`/shop/payment/${orderId}`)
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error('提交订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 检查是否有选中的商品
onMounted(() => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    router.push('/shop/cart')
  }
})
</script>

<style scoped>
.checkout-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.item:last-child {
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
  font-size: 16px;
  color: #f56c6c;
  font-weight: bold;
  width: 100px;
  text-align: right;
}

.order-summary {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  padding-top: 15px;
  margin-top: 15px;
  border-top: 1px solid #e4e7ed;
  font-weight: bold;
  color: #303133;
}

.price, .total-price {
  color: #f56c6c;
}

.total-price {
  font-size: 20px;
}

.checkout-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .checkout-container {
    padding: 10px;
  }
  
  .item-image {
    width: 60px;
    height: 60px;
  }
  
  .item-subtotal {
    width: 80px;
  }
}
</style> 