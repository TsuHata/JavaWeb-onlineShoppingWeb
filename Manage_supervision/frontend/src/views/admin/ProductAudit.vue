<template>
  <div class="container">
    <div class="page-header">
      <h2>商品审核</h2>
      <div class="status-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabChange">
          <el-tab-pane label="待审核" name="pending"></el-tab-pane>
          <el-tab-pane label="已通过" name="approved"></el-tab-pane>
          <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
          <el-tab-pane label="全部" name=""></el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 商品列表 -->
    <el-table :data="productList" v-loading="listLoading" border style="width: 100%">
      <el-table-column label="商品图片" width="100" align="center">
        <template #default="scope">
          <div v-if="scope.row.imageUrl" class="table-image-container">
            <!-- 假设多张图片以逗号分隔，获取第一张图片作为主图 -->
            <el-image 
              :src="getFirstImage(scope.row.imageUrl)" 
              :preview-src-list="getImageList(scope.row.imageUrl)"
              fit="cover" 
              style="width: 50px; height: 50px"
            ></el-image>
            <span v-if="getImageCount(scope.row.imageUrl) > 1" class="image-count">
              +{{ getImageCount(scope.row.imageUrl) - 1 }}
            </span>
          </div>
          <el-icon v-else><Picture /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" width="180"></el-table-column>
      <el-table-column prop="price" label="价格" width="80">
        <template #default="scope">
          <span>{{ scope.row.price ? `¥${scope.row.price}` : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80"></el-table-column>
      <el-table-column prop="categoryName" label="分类" width="120"></el-table-column>
      <el-table-column prop="merchantName" label="商家" width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
      <el-table-column label="操作" fixed="right" width="150">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            @click="handleViewProduct(scope.row)"
            plain
          >查看</el-button>
          <el-button 
            v-if="scope.row.status === 'pending'"
            size="small" 
            type="warning" 
            @click="handleAuditProduct(scope.row)"
            plain
          >审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="listQuery.size"
        :current-page="listQuery.page"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>

    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="商品详情"
      width="600px"
    >
      <div v-if="currentProduct" class="product-detail">
        <div class="product-gallery" v-if="currentProduct.imageUrl">
          <!-- 主图 -->
          <div class="main-image">
            <el-image 
              :src="getFirstImage(currentProduct.imageUrl)" 
              fit="cover"
              :preview-src-list="getImageList(currentProduct.imageUrl)"
            ></el-image>
          </div>
          
          <!-- 缩略图列表 -->
          <div class="thumbnail-list" v-if="getImageCount(currentProduct.imageUrl) > 1">
            <div 
              v-for="(img, index) in getImageList(currentProduct.imageUrl)" 
              :key="index" 
              class="thumbnail"
              :class="{ active: index === 0 }"
            >
              <el-image :src="img" fit="cover"></el-image>
            </div>
          </div>
        </div>

        <div class="detail-item">
          <span class="label">商品名称:</span>
          <span class="value">{{ currentProduct.name }}</span>
        </div>

        <div class="detail-item">
          <span class="label">商家名称:</span>
          <span class="value">{{ currentProduct.merchantName }}</span>
        </div>

        <div class="detail-item">
          <span class="label">商品分类:</span>
          <span class="value">{{ currentProduct.categoryName }}</span>
        </div>

        <div class="detail-item">
          <span class="label">商品价格:</span>
          <span class="value">¥{{ currentProduct.price }}</span>
        </div>

        <div class="detail-item">
          <span class="label">商品库存:</span>
          <span class="value">{{ currentProduct.stock }}</span>
        </div>

        <div class="detail-item">
          <span class="label">审核状态:</span>
          <span class="value">
            <el-tag :type="getStatusType(currentProduct.status)">
              {{ getStatusText(currentProduct.status) }}
            </el-tag>
          </span>
        </div>

        <div class="detail-item" v-if="currentProduct.auditComment">
          <span class="label">审核意见:</span>
          <span class="value">{{ currentProduct.auditComment }}</span>
        </div>

        <div class="detail-item">
          <span class="label">创建时间:</span>
          <span class="value">{{ currentProduct.createTime }}</span>
        </div>

        <div class="detail-item">
          <span class="label">更新时间:</span>
          <span class="value">{{ currentProduct.updateTime }}</span>
        </div>

        <div class="detail-item description">
          <span class="label">商品描述:</span>
          <div class="value">{{ currentProduct.description }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditVisible"
      title="商品审核"
      width="500px"
    >
      <div v-if="currentProduct" class="audit-form">
        <div class="product-summary">
          <div class="summary-images" v-if="currentProduct.imageUrl">
            <!-- 主图 -->
            <el-image 
              :src="getFirstImage(currentProduct.imageUrl)"
              fit="cover" 
              style="width: 80px; height: 80px"
              :preview-src-list="getImageList(currentProduct.imageUrl)"
            ></el-image>
            
            <!-- 其他图片缩略图 -->
            <div class="mini-thumbnails" v-if="getImageCount(currentProduct.imageUrl) > 1">
              <el-tooltip content="查看所有图片" placement="top">
                <div class="thumbnail-count">+{{ getImageCount(currentProduct.imageUrl) - 1 }}</div>
              </el-tooltip>
            </div>
          </div>
          <div class="summary-content">
            <h3>{{ currentProduct.name }}</h3>
            <p>商家: {{ currentProduct.merchantName }}</p>
            <p>价格: ¥{{ currentProduct.price }}</p>
          </div>
        </div>

        <el-form :model="auditForm" label-width="100px">
          <el-form-item label="审核结果">
            <el-radio-group v-model="auditForm.status">
              <el-radio label="approved" value="approved">通过</el-radio>
              <el-radio label="rejected" value="rejected">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="审核意见">
            <el-input
              v-model="auditForm.auditComment"
              type="textarea"
              :rows="4"
              placeholder="请输入审核意见"
            ></el-input>
          </el-form-item>
        </el-form>

        <div class="audit-actions">
          <el-button @click="auditVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit">确定</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { 
  getAllProducts,
  getPendingProducts,
  auditProduct
} from '../../api/product'

// 商品列表相关
const activeTab = ref('pending')
const productList = ref([])
const listLoading = ref(false)
const total = ref(0)
const listQuery = reactive({
  page: 1,
  size: 10,
  status: 'pending' // 默认显示待审核的商品
})

// 详情对话框
const detailVisible = ref(false)
const currentProduct = ref(null)

// 审核对话框
const auditVisible = ref(false)
const auditForm = reactive({
  productId: null,
  status: 'approved',
  auditComment: ''
})

// 获取商品列表
const fetchProducts = async () => {
  listLoading.value = true
  try {
    const response = await getAllProducts(
      listQuery.page,
      listQuery.size,
      listQuery.status
    )
    console.log('管理员API响应数据:', response)
    
    // 处理响应数据以获取正确的数组
    if (response.data && response.data.success) {
      // 处理新格式：{success: true, data: [...]}
      const actualPayload = response.data.data;
      
      if (Array.isArray(actualPayload)) {
        productList.value = actualPayload;
        total.value = actualPayload.length;
      } else if (actualPayload && typeof actualPayload === 'object') {
        if (actualPayload.items !== undefined && actualPayload.total !== undefined) {
          productList.value = actualPayload.items;
          total.value = actualPayload.total;
        } else if (actualPayload.content !== undefined && actualPayload.total !== undefined) {
          productList.value = actualPayload.content;
          total.value = actualPayload.total;
        } else if (actualPayload.records !== undefined && actualPayload.total !== undefined) {
          productList.value = actualPayload.records;
          total.value = actualPayload.total;
        } else {
          productList.value = [];
          total.value = 0;
          console.error('未知的响应数据结构 (actualPayload):', actualPayload);
        }
      } else {
        productList.value = [];
        total.value = 0;
        console.error('未知的响应数据类型 (actualPayload):', actualPayload);
      }
    } else {
      // 处理旧格式
      if (Array.isArray(response)) {
        productList.value = response;
        total.value = response.length;
      } else if (response.data && Array.isArray(response.data)) {
        productList.value = response.data;
        total.value = response.data.length;
      } else if (response.items) {
        productList.value = response.items;
        total.value = response.total;
      } else if (response.content) {
        productList.value = response.content;
        total.value = response.total;
      } else if (response.data && response.data.items) {
        productList.value = response.data.items;
        total.value = response.data.total;
      } else if (response.data && response.data.content) {
        productList.value = response.data.content;
        total.value = response.data.total;
      } else if (response.data && response.data.records) {
        productList.value = response.data.records;
        total.value = response.data.total;
      } else {
        productList.value = [];
        total.value = 0;
        console.error('未知的响应数据格式:', response);
      }
    }
    
    console.log('处理后的商品列表:', productList.value)
  } catch (error) {
    console.error('获取商品数据失败:', error)
    ElMessage.error('获取商品数据失败')
    productList.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

// 处理标签页切换
const handleTabChange = () => {
  listQuery.status = activeTab.value
  listQuery.page = 1 // 重置页码
  fetchProducts()
}

// 处理页码变化
const handlePageChange = (page) => {
  listQuery.page = page
  fetchProducts()
}

// 查看商品详情
const handleViewProduct = (row) => {
  currentProduct.value = row
  detailVisible.value = true
}

// 审核商品
const handleAuditProduct = (row) => {
  currentProduct.value = row
  auditForm.productId = row.id
  auditForm.status = 'approved' // 默认通过
  auditForm.auditComment = ''
  auditVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  try {
    await auditProduct({
      productId: auditForm.productId,
      status: auditForm.status,
      auditComment: auditForm.auditComment
    })
    ElMessage.success('审核操作成功')
    auditVisible.value = false
    fetchProducts() // 刷新商品列表
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 获取状态文字
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待审核'
    case 'approved':
      return '已通过'
    case 'rejected':
      return '已拒绝'
    default:
      return '未知状态'
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'approved':
      return 'success'
    case 'rejected':
      return 'danger'
    default:
      return 'info'
  }
}

// 添加图片处理方法
// 获取多图中的第一张图片作为主图
const getFirstImage = (imageUrl) => {
  if (!imageUrl) return '';
  return imageUrl.split(',')[0];
}

// 获取所有图片列表用于预览
const getImageList = (imageUrl) => {
  if (!imageUrl) return [];
  return imageUrl.split(',').filter(url => url.trim() !== '');
}

// 获取图片数量
const getImageCount = (imageUrl) => {
  if (!imageUrl) return 0;
  return imageUrl.split(',').filter(url => url.trim() !== '').length;
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.status-tabs {
  margin-top: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.product-detail {
  padding: 0 20px;
}

.product-gallery {
  margin-bottom: 20px;
}

.main-image {
  width: 100%;
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
  overflow: hidden;
  background: #f5f7fa;
  border-radius: 4px;
}

.main-image .el-image {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
}

.thumbnail-list {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.thumbnail {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.thumbnail.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.thumbnail .el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.table-image-container {
  position: relative;
}

.image-count {
  position: absolute;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 8px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
}

.detail-item .label {
  width: 100px;
  font-weight: bold;
  color: #606266;
}

.detail-item .value {
  flex: 1;
  color: #303133;
}

.detail-item.description {
  display: block;
}

.detail-item.description .label {
  display: block;
  margin-bottom: 10px;
}

.detail-item.description .value {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
  white-space: pre-wrap;
}

.product-summary {
  display: flex;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.summary-images {
  position: relative;
  margin-right: 15px;
}

.mini-thumbnails {
  position: absolute;
  right: 0;
  bottom: 0;
}

.thumbnail-count {
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  border-radius: 4px;
  padding: 1px 5px;
  font-size: 12px;
  cursor: pointer;
}

.summary-content {
  margin-left: 15px;
}

.summary-content h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.summary-content p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.audit-actions {
  text-align: right;
  margin-top: 20px;
}
</style> 