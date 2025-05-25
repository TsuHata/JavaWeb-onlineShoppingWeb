<template>
  <div class="merchant-management">
    <div class="page-header">
      <h2>商家管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商家姓名/用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商家列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="merchantList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNumber" label="商家编号" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'active' ? 'success' : 'danger'"
            >
              {{ row.status === 'active' ? '活跃' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewMerchantDetails(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 商家详情对话框 -->
    <el-dialog
      title="商家详情"
      v-model="detailsDialogVisible"
      width="600px"
    >
      <div v-if="selectedMerchant" class="merchant-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ selectedMerchant.id }}</el-descriptions-item>
          <el-descriptions-item label="商家编号">{{ selectedMerchant.userNumber }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedMerchant.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ selectedMerchant.realName }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedMerchant.email }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedMerchant.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedMerchant.status === 'active' ? 'success' : 'danger'">
              {{ selectedMerchant.status === 'active' ? '活跃' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedMerchant.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMerchants } from '../../api/merchant'
import type { User } from '../../api/user'

// 状态
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const merchantList = ref<User[]>([])
const detailsDialogVisible = ref(false)
const selectedMerchant = ref<User | null>(null)

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 方法
const fetchMerchants = async () => {
  loading.value = true
  try {
    const response = await getMerchants(currentPage.value, pageSize.value, searchForm.keyword)
    merchantList.value = response.content
    total.value = response.total
  } catch (error) {
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchMerchants()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchMerchants()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchMerchants()
}

const handleViewMerchantDetails = (merchant: User) => {
  selectedMerchant.value = merchant
  detailsDialogVisible.value = true
}

// 生命周期钩子
onMounted(() => {
  fetchMerchants()
})
</script>

<style scoped>
.merchant-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.merchant-info {
  padding: 10px;
}

.empty-data {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}
</style> 