<template>
  <div class="container">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openAddDialog(null)">添加根分类</el-button>
    </div>

    <div class="content-container">
      <div class="tree-container">
        <el-tree
          ref="treeRef"
          :data="categoryTree"
          node-key="id"
          default-expand-all
          :props="{
            label: 'name',
            children: 'children'
          }"
          :expand-on-click-node="false"
        >
          <template #default="{ node, data }">
            <div class="custom-tree-node">
              <span>{{ node.label }}</span>
              <div class="node-actions">
                <el-button
                  type="primary"
                  size="small"
                  circle
                  @click.stop="openAddDialog(data)"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  circle
                  @click.stop="openEditDialog(data)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  circle
                  @click.stop="confirmDelete(data)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="父级分类" v-if="!isEdit">
          <el-input
            disabled
            :value="parentCategory ? parentCategory.name : '无（根分类）'"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getCategoriesTree, createCategory, updateCategory, deleteCategory } from '../../api/category'

// 分类树数据
const categoryTree = ref([])
const loading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const parentCategory = ref(null)
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined,
  name: '',
  parentId: null
})
const formRules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度在 2 到 50 个字符之间', trigger: 'blur' }
  ]
})

// 获取分类树数据
const fetchCategories = async () => {
  loading.value = true
  try {
    const response = await getCategoriesTree()
    
    // 处理响应数据以获取正确的数组
    if (response.data && response.data.success) {
      // 处理新格式：{success: true, data: [...]}
      categoryTree.value = response.data.data
    } else if (response.data && Array.isArray(response.data)) {
      // 处理旧格式：直接返回数组
      categoryTree.value = response.data
    } else if (Array.isArray(response)) {
      // 极端情况：直接是数组
      categoryTree.value = response
    } else {
      console.error('未知的分类数据格式:', response)
      categoryTree.value = []
      ElMessage.error('获取分类数据格式错误')
    }
  } catch (error) {
    ElMessage.error('获取分类数据失败')
    console.error('获取分类数据失败:', error)
    categoryTree.value = []
  } finally {
    loading.value = false
  }
}

// 打开添加分类对话框
const openAddDialog = (parent) => {
  isEdit.value = false
  parentCategory.value = parent
  formData.parentId = parent ? parent.id : null
  dialogVisible.value = true
}

// 打开编辑分类对话框
const openEditDialog = (category) => {
  isEdit.value = true
  formData.id = category.id
  formData.name = category.name
  formData.parentId = category.parentId
  dialogVisible.value = true
}

// 确认删除分类
const confirmDelete = (category) => {
  ElMessageBox.confirm(
    `确定要删除分类 "${category.name}" 吗？如果该分类下有子分类，将一并删除！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await deleteCategory(category.id)
        ElMessage.success('删除成功')
        fetchCategories()
      } catch (error) {
        ElMessage.error('删除失败')
        console.error('删除失败:', error)
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateCategory(formData.id, {
            name: formData.name,
            parentId: formData.parentId
          })
          ElMessage.success('更新成功')
        } else {
          await createCategory({
            name: formData.name,
            parentId: formData.parentId
          })
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchCategories()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
        console.error(isEdit.value ? '更新失败:' : '添加失败:', error)
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.id = undefined
  formData.name = ''
  formData.parentId = null
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.container {
  padding: 20px;
  background-color: #fff;
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

.content-container {
  display: flex;
  justify-content: space-between;
}

.tree-container {
  width: 100%;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-actions {
  display: flex;
  gap: 5px;
}
</style> 