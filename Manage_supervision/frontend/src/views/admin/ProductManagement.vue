<template>
  <div class="container">
    <div class="page-header">
      <h2>商品管理</h2>
      <div class="filter-buttons">
        <el-button type="primary" @click="showFilterDrawer = true">高级筛选</el-button>
        <el-button @click="resetFilters">重置筛选</el-button>
      </div>
    </div>

    <!-- 基础筛选区域 -->
    <div class="filter-bar">
      <div class="filter-item">
        <span class="filter-label">状态：</span>
        <el-select v-model="listQuery.status" placeholder="状态筛选" clearable @change="handleFilterChange">
          <el-option label="全部" value=""></el-option>
          <el-option label="待审核" value="pending"></el-option>
          <el-option label="已通过" value="approved"></el-option>
          <el-option label="已拒绝" value="rejected"></el-option>
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">商家：</span>
        <el-select 
          v-model="listQuery.merchantId" 
          placeholder="选择商家" 
          clearable 
          filterable 
          @change="handleFilterChange"
        >
          <el-option 
            v-for="item in merchantOptions" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value"
          ></el-option>
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">分类：</span>
        <el-select 
          v-model="listQuery.categoryId" 
          placeholder="选择分类" 
          clearable 
          filterable 
          @change="handleFilterChange"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
      </div>
      
      <div class="filter-item">
        <el-input
          v-model="listQuery.keyword"
          placeholder="搜索商品名称/描述"
          clearable
          @keyup.enter="handleFilterChange"
        >
          <template #suffix>
            <el-icon class="el-input__icon" @click="handleFilterChange">
              <Search />
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 高级筛选抽屉 -->
    <el-drawer
      v-model="showFilterDrawer"
      title="高级筛选"
      direction="rtl"
      size="400px"
    >
      <div class="advanced-filter-container">
        <div class="filter-group">
          <h3>价格区间</h3>
          <div class="price-range">
            <el-input-number 
              v-model="listQuery.minPrice" 
              :min="0" 
              :precision="2" 
              :step="10" 
              placeholder="最低价" 
            />
            <span class="range-separator">至</span>
            <el-input-number 
              v-model="listQuery.maxPrice" 
              :min="0" 
              :precision="2" 
              :step="10" 
              placeholder="最高价" 
            />
          </div>
        </div>

        <div class="filter-group">
          <h3>库存区间</h3>
          <div class="stock-range">
            <el-input-number 
              v-model="listQuery.minStock" 
              :min="0" 
              :precision="0" 
              :step="10" 
              placeholder="最低库存" 
            />
            <span class="range-separator">至</span>
            <el-input-number 
              v-model="listQuery.maxStock" 
              :min="0" 
              :precision="0" 
              :step="10" 
              placeholder="最高库存" 
            />
          </div>
        </div>

        <div class="filter-group">
          <h3>创建时间</h3>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </div>

        <div class="filter-group">
          <h3>排序方式</h3>
          <el-select v-model="listQuery.sortBy" placeholder="排序字段" style="width: 100%; margin-bottom: 10px;">
            <el-option label="ID" value="id"></el-option>
            <el-option label="商品名称" value="name"></el-option>
            <el-option label="价格" value="price"></el-option>
            <el-option label="库存" value="stock"></el-option>
            <el-option label="创建时间" value="createTime"></el-option>
          </el-select>
          
          <el-radio-group v-model="listQuery.sortDirection" style="margin-top: 10px;">
            <el-radio label="asc" value="asc">升序</el-radio>
            <el-radio label="desc" value="desc">降序</el-radio>
          </el-radio-group>
        </div>

        <div class="drawer-footer">
          <el-button @click="resetFilters">重置筛选</el-button>
          <el-button type="primary" @click="applyAdvancedFilters">应用筛选</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 商品列表 -->
    <el-table 
      :data="productList" 
      v-loading="listLoading" 
      border 
      style="width: 100%"
      row-key="id"
    >
      <el-table-column label="商品图片" width="100" align="center">
        <template #default="scope">
          <div v-if="scope.row.imageUrl" class="table-image-container">
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
      <el-table-column prop="name" label="商品名称" min-width="140"></el-table-column>
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
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            @click="handleViewProduct(scope.row)"
            plain
          >查看</el-button>
          <el-button 
            size="small" 
            type="success" 
            @click="handleEditProduct(scope.row)"
            plain
          >编辑</el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="handleDeleteProduct(scope.row)"
            plain
          >删除</el-button>
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

    <!-- 编辑商品对话框 -->
    <el-dialog
      v-model="editVisible"
      title="编辑商品"
      width="700px"
    >
      <el-form 
        v-if="currentProduct" 
        :model="editForm" 
        label-width="120px" 
        ref="editFormRef"
        :rules="rules"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number 
            v-model="editForm.price" 
            :precision="2" 
            :step="0.1" 
            :min="0"
            style="width: 100%;"
          ></el-input-number>
        </el-form-item>

        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="editForm.stock" :min="0" :step="1" style="width: 100%;"></el-input-number>
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="商品所属商家" prop="merchantId">
          <el-select v-model="editForm.merchantId" placeholder="请选择商家" style="width: 100%;" filterable>
            <el-option
              v-for="item in merchantOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="商品图片">
          <div class="image-uploader-container">
            <div 
              v-for="(url, index) in editForm.imageUrls" 
              :key="index" 
              class="image-item"
            >
              <el-image :src="url" fit="cover" style="width: 100%; height: 100%;"></el-image>
              <div class="image-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  circle 
                  @click="removeImage(index)"
                  icon="Delete"
                ></el-button>
              </div>
            </div>
            
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleImageUpload"
            >
              <div class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <div>点击上传</div>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入商品描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Search, Plus, Delete } from '@element-plus/icons-vue'
import { 
  getAllProducts,
  adminUpdateProduct,
  adminDeleteProduct,
  uploadProductImage
} from '../../api/product'
import { getCategories } from '../../api/category'
import { getMerchants } from '../../api/merchant'

// 商品列表相关
const productList = ref([])
const listLoading = ref(false)
const total = ref(0)
const listQuery = reactive({
  page: 1,
  size: 10,
  status: '', // 状态筛选
  merchantId: null, // 商家筛选
  categoryId: null, // 分类筛选
  minPrice: null, // 最低价格
  maxPrice: null, // 最高价格
  minStock: null, // 最低库存
  maxStock: null, // 最高库存
  startDate: '', // 开始日期
  endDate: '', // 结束日期
  keyword: '', // 关键字搜索
  sortBy: 'id', // 排序字段
  sortDirection: 'desc' // 排序方向
})

// 日期范围选择器
const dateRange = ref([])

// 高级筛选抽屉
const showFilterDrawer = ref(false)

// 详情对话框
const detailVisible = ref(false)
const currentProduct = ref(null)

// 编辑对话框
const editVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  name: '',
  price: 0,
  stock: 0,
  categoryId: null,
  merchantId: null,
  description: '',
  imageUrls: [], // 多图展示
  status: ''
})

// 分类和商家选项
const categoryOptions = ref([])
const merchantOptions = ref([])

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  merchantId: [
    { required: true, message: '请选择商品所属商家', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' }
  ]
}

// 监听日期范围变化
watch(dateRange, (newVal) => {
  if (newVal) {
    listQuery.startDate = newVal[0] || ''
    listQuery.endDate = newVal[1] || ''
  } else {
    listQuery.startDate = ''
    listQuery.endDate = ''
  }
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategories()
    
    if (response.data && response.data.success) {
      const categoriesData = response.data.data;
      categoryOptions.value = categoriesData.map(item => ({
        value: item.id,
        label: item.name
      }))
    } else if (response.data && Array.isArray(response.data)) {
      categoryOptions.value = response.data.map(item => ({
        value: item.id,
        label: item.name
      }))
    } else if (Array.isArray(response)) {
      categoryOptions.value = response.map(item => ({
        value: item.id,
        label: item.name
      }))
    } else {
      console.error('获取分类失败: 未知的响应格式', response)
      categoryOptions.value = []
    }
  } catch (error) {
    console.error('获取分类失败:', error)
    categoryOptions.value = []
  }
}

// 获取商家列表
const fetchMerchants = async () => {
  try {
    const response = await getMerchants()
    
    if (response.data && response.data.success) {
      const merchantsData = response.data.data;
      merchantOptions.value = merchantsData.map(item => ({
        value: item.id,
        label: item.username
      }))
    } else if (response.data && Array.isArray(response.data)) {
      merchantOptions.value = response.data.map(item => ({
        value: item.id,
        label: item.username
      }))
    } else if (Array.isArray(response)) {
      merchantOptions.value = response.map(item => ({
        value: item.id,
        label: item.username
      }))
    } else {
      console.error('获取商家列表失败: 未知的响应格式', response)
      merchantOptions.value = []
    }
  } catch (error) {
    console.error('获取商家列表失败:', error)
    merchantOptions.value = []
  }
}

// 获取商品列表
const fetchProducts = async () => {
  listLoading.value = true
  try {
    // 构建查询参数
    const params = {
      page: listQuery.page,
      size: listQuery.size,
      status: listQuery.status,
      merchantId: listQuery.merchantId,
      categoryId: listQuery.categoryId,
      minPrice: listQuery.minPrice,
      maxPrice: listQuery.maxPrice,
      minStock: listQuery.minStock,
      maxStock: listQuery.maxStock,
      startDate: listQuery.startDate,
      endDate: listQuery.endDate,
      keyword: listQuery.keyword,
      sortBy: listQuery.sortBy,
      sortDirection: listQuery.sortDirection
    }
    
    // 移除值为null或空字符串的参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') {
        delete params[key]
      }
    })
    
    const response = await getAllProducts(
      params.page,
      params.size,
      params.status,
      params.merchantId,
      params.categoryId,
      params.minPrice,
      params.maxPrice,
      params.minStock,
      params.maxStock,
      params.startDate,
      params.endDate,
      params.keyword,
      params.sortBy,
      params.sortDirection
    )
    
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
      if (response.items) {
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
      } else if (Array.isArray(response)) {
        productList.value = response;
        total.value = response.length;
      } else if (response.data && Array.isArray(response.data)) {
        productList.value = response.data;
        total.value = response.data.length;
      } else {
        productList.value = [];
        total.value = 0;
        console.error('未知的响应数据格式:', response);
      }
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    productList.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

// 处理基础筛选条件变化
const handleFilterChange = () => {
  listQuery.page = 1 // 重置页码
  fetchProducts()
}

// 应用高级筛选
const applyAdvancedFilters = () => {
  listQuery.page = 1 // 重置页码
  showFilterDrawer.value = false
  fetchProducts()
}

// 重置所有筛选条件
const resetFilters = () => {
  // 重置基础筛选条件
  listQuery.status = ''
  listQuery.merchantId = null
  listQuery.categoryId = null
  listQuery.keyword = ''
  
  // 重置高级筛选条件
  listQuery.minPrice = null
  listQuery.maxPrice = null
  listQuery.minStock = null
  listQuery.maxStock = null
  listQuery.startDate = ''
  listQuery.endDate = ''
  listQuery.sortBy = 'id'
  listQuery.sortDirection = 'desc'
  
  // 重置日期范围选择器
  dateRange.value = []
  
  // 重置页码并重新获取数据
  listQuery.page = 1
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

// 编辑商品
const handleEditProduct = (row) => {
  currentProduct.value = row
  // 重置表单
  editForm.id = row.id
  editForm.name = row.name
  editForm.price = row.price
  editForm.stock = row.stock
  editForm.categoryId = row.categoryId
  editForm.merchantId = row.merchantId
  editForm.description = row.description
  editForm.status = row.status
  editForm.imageUrls = getImageList(row.imageUrl)
  
  editVisible.value = true
}

// 删除商品
const handleDeleteProduct = (row) => {
  ElMessageBox.confirm(
    `确定要删除商品 "${row.name}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await adminDeleteProduct(row.id)
      ElMessage.success('删除成功')
      fetchProducts() // 刷新列表
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 图片上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = /^image\//.test(file.type)
  const isLt2M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('上传文件只能是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 自定义上传处理
const handleImageUpload = async (options) => {
  try {
    const file = options.file
    const imageUrl = await uploadProductImage(file)
    editForm.imageUrls.push(imageUrl)
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

// 移除图片
const removeImage = (index) => {
  editForm.imageUrls.splice(index, 1)
}

// 提交编辑表单
const submitEditForm = async () => {
  if (!editFormRef.value) return
  
  editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 将多张图片URL合并为一个字符串，以逗号分隔
        const imageUrl = editForm.imageUrls.join(',')
        
        const data = {
          id: editForm.id,
          name: editForm.name,
          price: editForm.price,
          stock: editForm.stock,
          categoryId: editForm.categoryId,
          merchantId: editForm.merchantId,
          description: editForm.description,
          imageUrl,
          status: editForm.status // 保留原状态
        }
        
        await adminUpdateProduct(editForm.id, data)
        ElMessage.success('商品更新成功')
        editVisible.value = false
        fetchProducts() // 刷新列表
      } catch (error) {
        console.error('更新失败:', error)
        ElMessage.error('商品更新失败')
      }
    } else {
      ElMessage.warning('请完善表单信息')
      return false
    }
  })
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

// 图片处理方法
// 获取多图中的第一张图片作为主图
const getFirstImage = (imageUrl) => {
  if (!imageUrl) return ''
  return imageUrl.split(',')[0]
}

// 获取所有图片列表用于预览
const getImageList = (imageUrl) => {
  if (!imageUrl) return []
  return imageUrl.split(',').filter(url => url.trim() !== '')
}

// 获取图片数量
const getImageCount = (imageUrl) => {
  if (!imageUrl) return 0
  return imageUrl.split(',').filter(url => url.trim() !== '').length
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchMerchants()
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 15px;
  background-color: #f7f9fc;
  border-radius: 4px;
  margin-bottom: 20px;
}

.filter-item {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 8px;
  white-space: nowrap;
  color: #606266;
}

.advanced-filter-container {
  padding: 20px;
}

.filter-group {
  margin-bottom: 25px;
}

.filter-group h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.price-range, .stock-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.range-separator {
  color: #909399;
}

.drawer-footer {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  position: absolute;
  bottom: 20px;
  right: 20px;
  left: 20px;
  padding-top: 10px;
  border-top: 1px solid #dcdfe6;
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

/* 图片上传样式 */
.image-uploader-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.image-actions {
  position: absolute;
  top: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.4);
  padding: 4px;
  border-bottom-left-radius: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.upload-placeholder {
  text-align: center;
  color: #909399;
}

.upload-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

/* 响应式布局调整 */
@media screen and (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-item {
    width: 100%;
  }
  
  .filter-item .el-select,
  .filter-item .el-input {
    width: 100%;
  }
}
</style> 