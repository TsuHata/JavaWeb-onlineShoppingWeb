<template>
  <div class="container">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="handleCreateProduct">添加商品</el-button>
    </div>

    <!-- 商品列表 -->
    <div class="filter-container">
      <el-select v-model="listQuery.status" placeholder="审核状态" clearable @change="fetchProducts">
        <el-option label="全部" value=""></el-option>
        <el-option label="待审核" value="pending"></el-option>
        <el-option label="已通过" value="approved"></el-option>
        <el-option label="已拒绝" value="rejected"></el-option>
      </el-select>
    </div>

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
      <el-table-column prop="name" label="商品名称" width="200"></el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">
          <span>{{ scope.row.price ? `¥${scope.row.price}` : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80"></el-table-column>
      <el-table-column prop="categoryName" label="分类" width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column label="操作" fixed="right" width="150">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            @click="handleViewProduct(scope.row)"
            plain
          >查看</el-button>
          <el-button 
            size="small" 
            type="warning" 
            @click="handleEditProduct(scope.row)"
            :disabled="scope.row.status === 'pending'"
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

    <!-- 商品表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '添加商品' : '编辑商品'"
      width="600px"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <div class="category-selector">
            <el-input
              v-model="selectedCategoryName"
              placeholder="请选择商品分类"
              readonly
              @click="showCategoryDialog"
            >
              <template #append>
                <el-button @click="showCategoryDialog">
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number 
            v-model="productForm.price" 
            :min="0" 
            :precision="2" 
            :step="0.1"
          ></el-input-number>
        </el-form-item>

        <el-form-item label="商品库存" prop="stock">
          <el-input-number 
            v-model="productForm.stock" 
            :min="0" 
            :precision="0" 
            :step="1"
          ></el-input-number>
        </el-form-item>

        <el-form-item label="商品图片">
          <div class="product-images">
            <!-- 已上传的图片列表 -->
            <div v-if="productImages.length > 0" class="image-list">
              <div v-for="(img, index) in productImages" :key="index" class="image-item">
                <el-image :src="img" fit="cover" class="preview-image" />
                <div class="image-actions">
                  <el-button type="danger" :icon="Delete" circle size="small" @click="removeImage(index)" />
                  <el-button v-if="index !== 0" type="primary" :icon="Top" circle size="small" @click="setMainImage(index)" title="设为主图" />
                </div>
              </div>
            </div>
            
            <!-- 上传按钮 -->
            <el-upload
              v-if="productImages.length < 5"
              class="image-uploader"
              action="#"
              :http-request="uploadImage"
              :show-file-list="false"
              :before-upload="beforeUpload"
              v-model:file-list="uploadFileList"
            >
              <div class="upload-box">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">上传图片</div>
              </div>
            </el-upload>
          </div>
          <div class="upload-tip">
            <p>支持jpg、png格式，最大5MB，最多5张图片</p>
            <p v-if="productImages.length > 0">第一张图片将作为主图显示</p>
          </div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          ></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <span>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

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

    <!-- 分类选择对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      title="选择商品分类"
      width="500px"
      destroy-on-close
    >
      <div class="category-dialog">
        <div class="category-search">
          <el-input
            v-model="categorySearchKeyword"
            placeholder="搜索分类"
            clearable
            @input="handleCategorySearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="category-path" v-if="!isSearching">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-if="categoryPath.length > 0" @click="goToRoot">全部分类</el-breadcrumb-item>
            <el-breadcrumb-item 
              v-for="(item, index) in categoryPath" 
              :key="item.id"
              @click="goToPathIndex(index)"
              :class="{ 'last': index === categoryPath.length - 1 }"
            >
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          
          <el-button 
            v-if="categoryPath.length > 0" 
            @click="goBack" 
            text
            type="primary"
            size="small"
          >
            <el-icon><Back /></el-icon> 返回上级
          </el-button>
        </div>
        
        <div class="category-list" v-loading="categoryLoading">
          <div v-if="isSearching">
            <h4>搜索结果</h4>
            <el-empty v-if="searchResults.length === 0" description="无搜索结果">
              <template #image>
                <img src="/placeholder-avatar.png" style="width: 80px; height: 80px" />
              </template>
            </el-empty>
            <el-radio-group v-model="selectedCategoryId">
              <div v-for="item in searchResults" :key="item.id" class="category-item">
                <el-radio :label="item.id" :value="item.id">{{ item.name }}</el-radio>
              </div>
            </el-radio-group>
          </div>
          <div v-else>
            <el-empty v-if="currentCategoryList.length === 0" description="暂无分类">
              <template #image>
                <img src="/placeholder-avatar.png" style="width: 80px; height: 80px" />
              </template>
            </el-empty>
            <el-radio-group v-model="selectedCategoryId">
              <div v-for="item in currentCategoryList" :key="item.id" class="category-item">
                <el-radio :label="item.id" :value="item.id">{{ item.name }}</el-radio>
                <el-button 
                  v-if="hasChildren(item)" 
                  @click="navigateToCategory(item)"
                  text
                  type="primary"
                  size="small"
                >
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-radio-group>
          </div>
        </div>
      </div>
      <template #footer>
        <span>
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCategorySelection">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Plus, Delete, Top, Search, Back, ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import { getCategoriesTree } from '../../api/category'
import { 
  getMerchantProducts, 
  createProduct, 
  updateProduct, 
  deleteProduct, 
  uploadProductImage,
  getMerchantProductDetail
} from '../../api/product'

// 商品列表相关
const productList = ref([])
const listLoading = ref(false)
const total = ref(0)
const listQuery = reactive({
  page: 1,
  size: 10,
  status: ''
})

// 分类选项
const categoryOptions = ref([])

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('create') // create or edit
const productForm = ref({
  id: undefined,
  name: '',
  description: '',
  imageUrl: '',
  price: 0,
  stock: 0,
  categoryId: null
})

// 表单验证规则
const rules = reactive({
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ]
})

const productFormRef = ref<any>(null)
const detailVisible = ref(false)
const currentProduct = ref(null)

// 分类选择相关
const categoryDialogVisible = ref(false)
const selectedCategoryName = ref('')
const selectedCategoryId = ref<number | null>(null)
const categoryPath = ref<any[]>([])
const currentCategoryList = ref<any[]>([])
const categoryLoading = ref(false)
const categorySearchKeyword = ref('')
const isSearching = ref(false)
const searchResults = ref<any[]>([])

// 在script setup中添加uploadFileList变量和productImages
const uploadFileList = ref([]);
const productImages = ref<string[]>([]);

// 获取商品列表
const fetchProducts = async () => {
  if (listLoading.value) return
  
  listLoading.value = true
  try {
    const response = await getMerchantProducts(
      listQuery.page,
      listQuery.size,
      listQuery.status
    )
    console.log('API响应数据:', response)
    
    // 先判断response本身的类型
    if (!response) {
      productList.value = []
      total.value = 0
      console.error('响应为空')
      return
    }
    
    // 响应数据直接是数组的情况
    if (Array.isArray(response)) {
      productList.value = response
      total.value = response.length
      console.log('处理后的商品列表(数组):', productList.value)
      return
    }
    
    // 处理各种可能的响应格式
    if (response.content !== undefined) {
      // 标准分页响应: { content: [...], total: 10, page: 1, size: 10 }
      productList.value = response.content
      total.value = response.total || response.content.length
    } else if (response.items !== undefined) {
      // 另一种分页格式: { items: [...], total: 10 }
      productList.value = response.items
      total.value = response.total || response.items.length
    } else if (response.records !== undefined) {
      // MyBatis-Plus分页格式: { records: [...], total: 10 }
      productList.value = response.records
      total.value = response.total || response.records.length
    } else if (response.data !== undefined) {
      // 封装格式: { data: {...}, success: true }
      const data = response.data
      
      if (Array.isArray(data)) {
        // 数组类型直接使用
        productList.value = data
        total.value = data.length
      } else if (data && typeof data === 'object') {
        // 对象类型，继续解析
        if (data.content !== undefined) {
          productList.value = data.content
          total.value = data.total || data.content.length
        } else if (data.items !== undefined) {
          productList.value = data.items
          total.value = data.total || data.items.length
        } else if (data.records !== undefined) {
          productList.value = data.records
          total.value = data.total || data.records.length
        } else {
          productList.value = []
          total.value = 0
          console.warn('未识别的数据格式(data):', data)
        }
      } else {
        productList.value = []
        total.value = 0
        console.warn('未识别的数据类型(data):', typeof data)
      }
    } else {
      // 无法识别的格式，记录日志但不触发错误
      productList.value = []
      total.value = 0
      console.warn('未识别的响应格式:', response)
    }
    
    console.log('处理后的商品列表:', productList.value)
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    productList.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategoriesTree();
    console.log('分类数据原始响应:', response);
    
    // 直接使用API返回的数据，API已经处理好格式
    let categoriesData = response || [];
    
    // 确保是数组
    if (!Array.isArray(categoriesData)) {
      console.warn('分类数据不是数组，重置为空数组');
      categoriesData = [];
    }
    
    categoryOptions.value = categoriesData;
    console.log('设置分类选项:', categoryOptions.value);
  } catch (error) {
    console.error('获取分类失败:', error);
    categoryOptions.value = [];
  }
}

// 处理页码变化
const handlePageChange = (page) => {
  listQuery.page = page
  fetchProducts()
}

// 添加商品
const handleCreateProduct = () => {
  dialogType.value = 'create'
  productForm.value = {
    id: undefined,
    name: '',
    description: '',
    imageUrl: '',
    price: 0,
    stock: 0,
    categoryId: null
  }
  // 清空图片列表
  productImages.value = [];
  dialogVisible.value = true
}

// 编辑商品
const handleEditProduct = (row) => {
  dialogType.value = 'edit'
  productForm.value = {
    id: row.id,
    name: row.name,
    description: row.description,
    imageUrl: row.imageUrl,
    price: row.price,
    stock: row.stock,
    categoryId: row.categoryId
  }
  
  // 填充图片列表
  productImages.value = [];
  if (row.imageUrl) {
    // 分割多张图片URL（假设用逗号分隔）
    const images = row.imageUrl.split(',').filter(img => img.trim() !== '');
    productImages.value = images;
  }
  
  // 设置分类名称
  selectedCategoryName.value = row.categoryName || '';
  selectedCategoryId.value = row.categoryId;
  
  // 确保分类数据已加载
  if (categoryOptions.value.length === 0) {
    fetchCategories().then(() => {
      updateSelectedCategoryName();
    });
  } else {
    updateSelectedCategoryName();
  }
  
  dialogVisible.value = true
}

// 查看商品详情
const handleViewProduct = async (row) => {
  try {
    const response = await getMerchantProductDetail(row.id)
    console.log('商品详情原始响应:', response)
    
    // 处理不同的响应格式
    if (!response) {
      ElMessage.error('获取商品详情失败: 响应为空')
      return
    }
    
    // 从响应中提取商品数据
    let productData = null
    
    if (response.data) {
      // 响应包含data字段
      productData = response.data
    } else {
      // 直接使用响应作为商品数据
      productData = response
    }
    
    // 确保数据是对象类型
    if (productData && typeof productData === 'object') {
      currentProduct.value = productData
      detailVisible.value = true
    } else {
      console.error('获取商品详情失败: 无效的数据格式', productData)
      ElMessage.error('获取商品详情失败: 数据格式错误')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

// 删除商品
const handleDeleteProduct = (row) => {
  ElMessageBox.confirm(
    `确定要删除商品 "${row.name}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await deleteProduct(row.id)
        ElMessage.success('删除成功')
        fetchProducts()
      } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 提交表单
const submitForm = async () => {
  if (!productForm.value) return
  
  productFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'create') {
          await createProduct(productForm.value)
          ElMessage.success('添加成功')
        } else {
          await updateProduct(productForm.value.id, productForm.value)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchProducts()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 上传图片前的验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 自定义上传方法
const uploadImage = async (options) => {
  try {
    console.log('开始上传文件:', options.file.name);
    
    // 检查图片数量限制
    if (productImages.value.length >= 5) {
      ElMessage.warning('最多只能上传5张图片');
      return;
    }
    
    // 上传图片
    const response = await uploadProductImage(options.file);
    console.log('上传API响应完成，数据:', response);
    
    // 手动处理响应，避免element-plus的回调问题
    if (response && response.imageUrl) {
      // 添加到图片列表
      productImages.value.push(response.imageUrl);
      
      // 设置第一张图片为商品主图
      if (productImages.value.length === 1) {
        productForm.value.imageUrl = response.imageUrl;
      } else {
        // 更新商品主图为所有图片的逗号分隔字符串
        productForm.value.imageUrl = productImages.value.join(',');
      }
      
      ElMessage.success('上传成功');
    } else {
      console.error('服务器响应缺少imageUrl:', response);
      ElMessage.error('上传响应格式错误');
    }
    
    // 仍然调用onSuccess以满足组件要求
    options.onSuccess();
  } catch (error) {
    console.error('上传失败:', error);
    options.onError(error);
    ElMessage.error('上传失败: ' + (error.message || '未知错误'));
  }
}

// 移除图片
const removeImage = (index) => {
  productImages.value.splice(index, 1);
  
  // 更新商品主图URL
  if (productImages.value.length > 0) {
    productForm.value.imageUrl = productImages.value.join(',');
  } else {
    productForm.value.imageUrl = '';
  }
}

// 设置主图
const setMainImage = (index) => {
  if (index >= 0 && index < productImages.value.length) {
    // 将选中的图片移到第一位
    const img = productImages.value[index];
    productImages.value.splice(index, 1);
    productImages.value.unshift(img);
    
    // 更新商品主图URL
    productForm.value.imageUrl = productImages.value.join(',');
    
    ElMessage.success('已设置为主图');
  }
}

// 修改上传成功回调，避免重复处理
const handleUploadSuccess = (response) => {
  // 不再在此处处理响应，因为已在uploadImage中处理
  console.log('触发上传成功回调');
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

// 显示分类选择对话框
const showCategoryDialog = async () => {
  // 先设置选中的分类ID
  selectedCategoryId.value = productForm.value.categoryId
  
  // 确保有分类数据
  if (categoryOptions.value.length === 0) {
    await fetchCategories()
  }
  
  // 更新分类名称显示
  updateSelectedCategoryName()
  
  // 打开对话框
  categoryDialogVisible.value = true
  
  // 初始化分类列表
  if (categoryPath.value.length === 0) {
    loadRootCategories()
  }
}

// 加载根分类
const loadRootCategories = async () => {
  categoryLoading.value = true;
  try {
    const response = await getCategoriesTree();
    console.log('获取到的分类树数据:', response);
    
    // 直接使用API返回的数据，API已经处理好格式
    let categoriesData = response || [];
    
    // 确保是数组
    if (!Array.isArray(categoriesData)) {
      console.warn('分类数据不是数组，重置为空数组');
      categoriesData = [];
    }
    
    // 提取根分类，不包含子分类
    currentCategoryList.value = categoriesData.map(item => ({
      id: item.id,
      name: item.name,
      hasChildren: !!item.children && item.children.length > 0,
      children: item.children
    }));
    
    console.log('处理后的根分类数据:', currentCategoryList.value);
    categoryPath.value = [];
  } catch (error) {
    console.error('获取分类数据失败:', error);
    ElMessage.error('获取分类数据失败');
    currentCategoryList.value = [];
  } finally {
    categoryLoading.value = false;
  }
}

// 判断分类是否有子分类
const hasChildren = (category) => {
  return category.hasChildren || (category.children && category.children.length > 0)
}

// 导航到指定分类
const navigateToCategory = (category) => {
  categoryPath.value.push({
    id: category.id,
    name: category.name
  });
  
  // 如果已经有缓存的子分类数据，直接使用
  if (category.children && category.children.length > 0) {
    currentCategoryList.value = category.children.map(item => ({
      id: item.id,
      name: item.name,
      hasChildren: !!item.children && item.children.length > 0,
      children: item.children
    }));
    console.log('使用缓存的子分类数据:', currentCategoryList.value);
  } else {
    // 否则重新获取子分类数据
    categoryLoading.value = true;
    getCategoriesTree(category.id).then(response => {
      console.log('获取到的子分类数据:', response);
      
      // 直接使用API返回的数据，API已经处理好格式
      let categoriesData = response || [];
      
      // 确保是数组
      if (!Array.isArray(categoriesData)) {
        console.warn('子分类数据不是数组，重置为空数组');
        categoriesData = [];
      }
      
      currentCategoryList.value = categoriesData.map(item => ({
        id: item.id,
        name: item.name,
        hasChildren: !!item.children && item.children.length > 0,
        children: item.children
      }));
      
      console.log('处理后的子分类数据:', currentCategoryList.value);
    }).catch(error => {
      console.error('获取子分类数据失败:', error);
      ElMessage.error('获取子分类数据失败');
      currentCategoryList.value = [];
    }).finally(() => {
      categoryLoading.value = false;
    });
  }
}

// 返回上一级
const goBack = () => {
  if (categoryPath.value.length > 0) {
    categoryPath.value.pop();
    
    if (categoryPath.value.length === 0) {
      // 返回根分类
      loadRootCategories();
    } else {
      // 返回上一级分类
      const parentId = categoryPath.value[categoryPath.value.length - 1].id;
      categoryLoading.value = true;
      getCategoriesTree(parentId).then(response => {
        console.log('返回上一级，获取的分类数据:', response);
        
        // 直接使用API返回的数据，API已经处理好格式
        let categoriesData = response || [];
        
        // 确保是数组
        if (!Array.isArray(categoriesData)) {
          console.warn('分类数据不是数组，重置为空数组');
          categoriesData = [];
        }
        
        currentCategoryList.value = categoriesData.map(item => ({
          id: item.id,
          name: item.name,
          hasChildren: !!item.children && item.children.length > 0,
          children: item.children
        }));
        
        console.log('返回上一级后的分类列表:', currentCategoryList.value);
      }).catch(error => {
        console.error('获取子分类数据失败:', error);
        ElMessage.error('获取子分类数据失败');
        currentCategoryList.value = [];
      }).finally(() => {
        categoryLoading.value = false;
      });
    }
  }
}

// 导航到路径中的特定索引
const goToPathIndex = (index) => {
  if (index < categoryPath.value.length - 1) {
    categoryPath.value = categoryPath.value.slice(0, index + 1);
    const categoryId = categoryPath.value[index].id;
    
    categoryLoading.value = true;
    getCategoriesTree(categoryId).then(response => {
      console.log('导航到指定索引，获取的分类数据:', response);
      
      // 直接使用API返回的数据，API已经处理好格式
      let categoriesData = response || [];
      
      // 确保是数组
      if (!Array.isArray(categoriesData)) {
        console.warn('分类数据不是数组，重置为空数组');
        categoriesData = [];
      }
      
      currentCategoryList.value = categoriesData.map(item => ({
        id: item.id,
        name: item.name,
        hasChildren: !!item.children && item.children.length > 0,
        children: item.children
      }));
      
      console.log('导航到指定索引后的分类列表:', currentCategoryList.value);
    }).catch(error => {
      console.error('获取子分类数据失败:', error);
      ElMessage.error('获取子分类数据失败');
      currentCategoryList.value = [];
    }).finally(() => {
      categoryLoading.value = false;
    });
  }
}

// 返回根分类
const goToRoot = () => {
  categoryPath.value = []
  loadRootCategories()
}

// 搜索分类
const handleCategorySearch = () => {
  if (categorySearchKeyword.value) {
    isSearching.value = true
    categoryLoading.value = true
    
    // 这里应使用后端搜索API，这里简化处理，使用前端搜索
    const searchInCategories = (categories, results = []) => {
      categories.forEach(category => {
        if (category.name.includes(categorySearchKeyword.value)) {
          results.push({
            id: category.id,
            name: category.name
          })
        }
        
        if (category.children && category.children.length > 0) {
          searchInCategories(category.children, results)
        }
      })
      return results
    }
    
    // 假设categoryOptions包含完整的分类树
    searchResults.value = searchInCategories(categoryOptions.value)
    categoryLoading.value = false
  } else {
    isSearching.value = false
  }
}

// 确认分类选择
const confirmCategorySelection = () => {
  if (selectedCategoryId.value) {
    productForm.value.categoryId = selectedCategoryId.value;
    
    // 在分类列表中查找分类名称
    const findCategoryById = (id, categories) => {
      for (const cat of categories) {
        if (cat.id === id) {
          return cat.name;
        }
        if (cat.children && cat.children.length > 0) {
          const found = findCategoryById(id, cat.children);
          if (found) return found;
        }
      }
      return null;
    };
    
    // 在当前分类列表中查找
    const categoryName = findCategoryById(selectedCategoryId.value, currentCategoryList.value);
    if (categoryName) {
      selectedCategoryName.value = categoryName;
    } else if (categoryPath.value.length > 0) {
      // 如果在当前分类列表中找不到，可能在路径中
      const lastPath = categoryPath.value[categoryPath.value.length - 1];
      if (lastPath.id === selectedCategoryId.value) {
        selectedCategoryName.value = lastPath.name;
      } else {
        // 尝试在分类选项中查找
        const nameFromOptions = findCategoryById(selectedCategoryId.value, categoryOptions.value);
        if (nameFromOptions) {
          selectedCategoryName.value = nameFromOptions;
        } else {
          console.warn('无法找到分类名称，使用ID显示');
          selectedCategoryName.value = `分类 #${selectedCategoryId.value}`;
        }
      }
    } else {
      // 尝试在搜索结果中查找
      if (isSearching.value && searchResults.value.length > 0) {
        const found = searchResults.value.find(item => item.id === selectedCategoryId.value);
        if (found) {
          selectedCategoryName.value = found.name;
        } else {
          console.warn('无法在搜索结果中找到分类名称，使用ID显示');
          selectedCategoryName.value = `分类 #${selectedCategoryId.value}`;
        }
      } else {
        // 尝试在分类选项中查找
        const nameFromOptions = findCategoryById(selectedCategoryId.value, categoryOptions.value);
        if (nameFromOptions) {
          selectedCategoryName.value = nameFromOptions;
        } else {
          console.warn('无法找到分类名称，使用ID显示');
          selectedCategoryName.value = `分类 #${selectedCategoryId.value}`;
        }
      }
    }
    
    console.log('选择的分类ID:', selectedCategoryId.value, '分类名称:', selectedCategoryName.value);
    categoryDialogVisible.value = false;
  } else {
    ElMessage.warning('请选择一个分类');
  }
}

// 更新已选分类名称
const updateSelectedCategoryName = () => {
  console.log('更新分类名称，当前ID:', productForm.value.categoryId);
  console.log('所有分类数据:', categoryOptions.value);
  
  if (!productForm.value.categoryId) {
    selectedCategoryName.value = '';
    return;
  }
  
  // 首先检查是否已经有categoryName可以直接使用
  if (productForm.value.categoryName) {
    selectedCategoryName.value = productForm.value.categoryName;
    console.log('使用现有分类名称:', selectedCategoryName.value);
    return;
  }
  
  const findCategoryName = (categories, id) => {
    if (!categories || categories.length === 0) return null;
    
    for (const category of categories) {
      if (category.id === id) {
        return category.name;
      }
      
      if (category.children && category.children.length > 0) {
        const name = findCategoryName(category.children, id);
        if (name) {
          return name;
        }
      }
    }
    return null;
  };
  
  const name = findCategoryName(categoryOptions.value, productForm.value.categoryId);
  if (name) {
    selectedCategoryName.value = name;
    console.log('找到分类名称:', name);
  } else {
    // 如果在分类树中找不到，可能需要重新加载分类数据
    console.log('在分类树中未找到ID为', productForm.value.categoryId, '的分类');
    selectedCategoryName.value = '';
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
  fetchCategories()
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
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.avatar-uploader {
  width: 178px;
  height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
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

.category-selector {
  width: 100%;
}

.category-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-path {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.category-list {
  min-height: 300px;
  max-height: 400px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.category-item:last-child {
  border-bottom: none;
}

:deep(.el-breadcrumb__item) {
  cursor: pointer;
}

:deep(.el-breadcrumb__item:last-child) {
  font-weight: bold;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #409eff;
}

.product-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 10px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  gap: 5px;
  padding: 5px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.image-uploader {
  width: 100px;
  height: 100px;
}

.upload-box {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.upload-box:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  font-size: 12px;
  color: #606266;
  margin-top: 8px;
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
</style> 