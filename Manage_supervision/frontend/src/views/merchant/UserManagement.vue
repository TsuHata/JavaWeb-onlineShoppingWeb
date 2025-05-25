<template>
  <div class="user-management">
    <!-- 如果没有用户管理权限，显示提示信息 -->
    <el-empty 
      v-if="!hasPermission('USER_MANAGEMENT')"
      description="您没有用户管理权限"
    >
      <template #image>
        <el-icon style="font-size: 48px;"><Lock /></el-icon>
      </template>
    </el-empty>

    <!-- 有权限才显示用户管理内容 -->
    <el-card class="box-card" v-if="hasPermission('USER_MANAGEMENT')">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="search-container">
            <el-input
              v-model="searchQuery"
              placeholder="搜索用户..."
              class="search-input"
              clearable
              @input="handleSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredUsers"
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="姓名" width="120">
          <template #default="scope">
            {{ scope.row.realName || scope.row.name || scope.row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="userNumber" label="用户编号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
              {{ scope.row.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewUserDetails(scope.row)"
              v-permission="'USER_VIEW'"
            >
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button 
              type="warning" 
              size="small"
              @click="toggleUserStatus(scope.row)"
              v-permission="'USER_EDIT'"
            >
              <el-icon><Lock /></el-icon>
              {{ scope.row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalUsers"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="用户详情"
      width="60%"
      destroy-on-close
      class="user-detail-dialog"
    >
      <div v-if="currentUser" class="user-info-container">
        <div class="user-header">
          <div class="avatar-container">
            <el-avatar :size="80" :src="currentUser.avatar">
              {{ (currentUser.realName || currentUser.name || currentUser.username || '').substring(0, 1) }}
            </el-avatar>
          </div>
          <div class="user-main-info">
            <h2>{{ currentUser.realName || currentUser.name || currentUser.username }}</h2>
            <div class="user-id">用户编号: {{ currentUser.userNumber }}</div>
          </div>
        </div>

        <el-divider />
        
        <!-- 用户基本信息部分 - 所有权限用户都可查看 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentUser.realName }}</el-descriptions-item>
          <el-descriptions-item label="用户编号">{{ currentUser.userNumber }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ currentUser.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 'active' ? 'success' : 'warning'">
              {{ currentUser.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentUser.createTime || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="角色" :span="2">
            <el-tag 
              v-for="(role, index) in currentUser.roles" 
              :key="index" 
              type="info" 
              class="mr-2"
            >
              {{ role }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ currentUser.bio || '暂无个人简介' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 用户进度部分 - 需要权限才能显示 -->
        <div v-if="hasPermission('USER_PROGRESS_VIEW')" class="user-progress mt-4">
          <h3>用户活动</h3>
          <el-divider />
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>总体活跃度</span>
                  </div>
                </template>
                <el-progress :percentage="75" :stroke-width="20" :format="percentFormat" :color="getProgressColor(75)"></el-progress>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>最近活跃度</span>
                  </div>
                </template>
                <el-progress :percentage="60" :stroke-width="20" :format="percentFormat" :color="getProgressColor(60)"></el-progress>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" class="mt-4">
            <el-col :span="24">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>近期活动</span>
                  </div>
                </template>
                <el-timeline>
                  <el-timeline-item
                    timestamp="2023-05-10 20:46"
                    type="success"
                  >
                    浏览商品：《智能手机》
                  </el-timeline-item>
                  <el-timeline-item
                    timestamp="2023-05-08 09:30"
                    type="primary"
                  >
                    参与在线客服咨询
                  </el-timeline-item>
                  <el-timeline-item
                    timestamp="2023-05-05 18:12"
                    type="warning"
                  >
                    完成订单：《服装类商品》
                  </el-timeline-item>
                </el-timeline>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Search,
  View,
  Edit,
  Delete,
  Lock
} from '@element-plus/icons-vue';
import { getUsers, getUserDetail, updateUserStatus, deleteUser, getAssignedUsers } from '../../api/user';
import type { User } from '../../api/user';
import { useRouter } from 'vue-router';
import { hasPermission } from '../../utils/permission';

// 状态变量
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalUsers = ref<number>(0);
const users = ref<User[]>([]);
const selectedUsers = ref<User[]>([]);
const currentUser = ref<User | null>(null);
const detailsDialogVisible = ref<boolean>(false);
const router = useRouter();

// 初始化数据
onMounted(() => {
  fetchUsers();
});

// 获取用户数据
const fetchUsers = async () => {
  loading.value = true;
  try {
    const data = await getAssignedUsers();
    console.log('获取的用户数据:', JSON.stringify(data));
    
    if (Array.isArray(data)) {
      console.log('第一条用户数据:', data.length > 0 ? JSON.stringify(data[0]) : '无数据');
      users.value = data;
      totalUsers.value = data.length;
      if (data.length > 0) {
        ElMessage.success('成功获取用户数据');
      } else {
        ElMessage.info('暂无分配的用户');
      }
    } else if (data && typeof data === 'object' && Array.isArray(data.content)) {
      console.log('分页用户数据第一条:', data.content.length > 0 ? JSON.stringify(data.content[0]) : '无数据');
      // 处理分页响应格式
      users.value = data.content;
      totalUsers.value = data.totalElements || data.content.length;
      if (data.content.length > 0) {
        ElMessage.success('成功获取用户数据');
      } else {
        ElMessage.info('暂无分配的用户');
      }
    } else {
      console.error('返回的用户数据格式不正确:', data);
      ElMessage.warning('获取的用户数据格式不正确');
      users.value = [];
      totalUsers.value = 0;
    }
  } catch (error) {
    console.error('获取用户数据失败:', error);
    ElMessage.error('获取用户数据失败，请稍后重试');
    users.value = [];
    totalUsers.value = 0;
  } finally {
    loading.value = false;
  }
};

// 根据搜索过滤用户
const filteredUsers = computed(() => {
  if (!Array.isArray(users.value)) {
    console.warn('用户数据不是数组:', users.value);
    return [];
  }
  
  if (!searchQuery.value) return users.value;
  
  const query = searchQuery.value.toLowerCase();
  return users.value.filter(user => 
    (user.realName && user.realName.toLowerCase().includes(query)) || 
    (user.name && user.name.toLowerCase().includes(query)) || 
    (user.username && user.username.toLowerCase().includes(query)) || 
    (user.userNumber && user.userNumber.toLowerCase().includes(query)) || 
    (user.email && user.email.toLowerCase().includes(query))
  );
});

// 处理函数
const handleSearch = () => {
  currentPage.value = 1;
};

const handleSelectionChange = (selection: User[]) => {
  selectedUsers.value = selection;
};

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize;
  currentPage.value = 1;
  fetchUsers();
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
  fetchUsers();
};

const viewUserDetails = async (user: User) => {
  // 检查权限
  if (!hasPermission('USER_VIEW')) {
    ElMessage.error('您没有查看用户详情的权限');
    return;
  }
  
  try {
    loading.value = true;
    console.log('查看用户详情, ID:', user.id);
    
    try {
      // 获取用户详细信息
      const detailedUser = await getUserDetail(user.id);
      console.log('获取到的详细用户信息:', detailedUser);
      
      // 确保所有必要的字段都存在
      const processedUser = {
        ...user,
        ...detailedUser,
        realName: detailedUser.realName || user.realName || user.name || '',
        username: detailedUser.username || user.username || '',
        userNumber: detailedUser.userNumber || user.userNumber || '',
        email: detailedUser.email || user.email || '',
        phone: detailedUser.phone || user.phone || '',
        status: detailedUser.status || user.status || 'inactive',
        createTime: detailedUser.createTime || user.createTime || '',
        roles: Array.isArray(detailedUser.roles) ? detailedUser.roles : 
              Array.isArray(user.roles) ? user.roles : ['USER'],
        bio: detailedUser.bio || ''
      };
      
      currentUser.value = processedUser;
      detailsDialogVisible.value = true;
    } catch (error) {
      // 如果getUserDetail失败，直接使用表格中的用户数据显示详情
      console.log('使用当前用户数据作为详情:', user);
      currentUser.value = {
        ...user,
        realName: user.realName || user.name || '',
        roles: Array.isArray(user.roles) ? user.roles : ['USER'],
        bio: ''
      };
      detailsDialogVisible.value = true;
    }
  } catch (error) {
    console.error('获取用户详情失败:', error);
    ElMessage.error('获取用户详情失败');
  } finally {
    loading.value = false;
  }
};

const toggleUserStatus = async (user: User) => {
  // 检查权限
  if (!hasPermission('USER_EDIT')) {
    ElMessage.error('您没有修改用户状态的权限');
    return;
  }
  
  try {
    const newStatus = user.status === 'active' ? 'inactive' : 'active';
    const success = await updateUserStatus(user.id, newStatus);
    
    if (success) {
      ElMessage.success(`用户${user.name}状态已${newStatus === 'active' ? '启用' : '禁用'}`);
      // 更新本地数据
      const index = users.value.findIndex(s => s.id === user.id);
      if (index !== -1) {
        users.value[index].status = newStatus;
      }
    } else {
      ElMessage.error('更新用户状态失败');
    }
  } catch (error) {
    console.error('更新用户状态失败:', error);
    ElMessage.error('更新用户状态失败');
  }
};

const deleteUserAccount = async (user: User) => {
  // 检查权限
  if (!hasPermission('USER_DELETE')) {
    ElMessage.error('您没有删除用户的权限');
    return;
  }
  
  try {
    const success = await deleteUser(user.id);
    
    if (success) {
      ElMessage.success(`用户${user.name}已删除`);
      // 从本地数据中移除
      users.value = users.value.filter(s => s.id !== user.id);
      totalUsers.value = users.value.length;
    } else {
      ElMessage.error('删除用户失败');
    }
  } catch (error) {
    console.error('删除用户失败:', error);
    ElMessage.error('删除用户失败');
  }
};

// 辅助函数
const percentFormat = (percentage: number): string => {
  return percentage ? `${percentage}%` : '0%';
};

const getProgressColor = (completion: number): string => {
  if (completion >= 80) return '#67C23A';
  if (completion >= 50) return '#409EFF';
  return '#E6A23C';
};

const getActivityType = (type: string): string => {
  switch (type) {
    case 'success': return 'success';
    case 'warning': return 'warning';
    case 'error': return 'danger';
    default: return 'primary';
  }
};
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-container {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 250px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.details-tabs {
  margin-top: 20px;
}

.mt-4 {
  margin-top: 16px;
}

.mr-2 {
  margin-right: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.user-detail-dialog {
  /* Add your styles here */
}

.user-info-container {
  padding: 0 20px;
}

.user-header {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.avatar-container {
  margin-right: 24px;
}

.user-main-info {
  flex: 1;
}

.user-main-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.user-id {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.user-roles {
  margin-top: 12px;
}
</style> 