<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h2>消息</h2>
          <el-dropdown @command="handleCommand">
            <el-button type="primary" size="small">
              新建聊天 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="showMerchantList">联系商家</el-dropdown-item>
                <el-dropdown-item command="showCustomerService">联系客服</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <ChatList 
          :loading="loadingConversations"
          @select="handleSelectConversation"
        />
      </div>
      <div class="chat-main">
        <ChatWindow 
          :conversation-id="activeConversationId"
          @message-sent="handleMessageSent"
        />
      </div>
    </div>

    <!-- 商家选择对话框 -->
    <el-dialog
      v-model="showMerchantDialog"
      title="选择商家"
      width="500px"
    >
      <el-input
        v-model="merchantSearchKeyword"
        placeholder="搜索商家"
        prefix-icon="Search"
        clearable
        @input="handleMerchantSearch"
      />
      
      <el-table
        :data="filteredMerchants"
        style="width: 100%; margin-top: 16px;"
        height="350px"
        v-loading="loadingMerchants"
      >
        <el-table-column prop="name" label="商家名称" width="180" />
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="startChatWithMerchant(scope.row)">
              开始聊天
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 客服对话框 -->
    <el-dialog
      v-model="showCustomerServiceDialog"
      title="联系客服"
      width="500px"
    >
      <div class="customer-service-info">
        <p>请选择您需要咨询的问题类型：</p>
        <el-radio-group v-model="customerServiceType">
          <el-radio label="product" value="product">商品咨询</el-radio>
          <el-radio label="order" value="order">订单问题</el-radio>
          <el-radio label="refund" value="refund">退款售后</el-radio>
          <el-radio label="other" value="other">其他问题</el-radio>
        </el-radio-group>
        
        <el-input
          v-model="customerServiceMessage"
          type="textarea"
          placeholder="请简单描述您的问题，以便我们更好地为您服务"
          :rows="3"
          style="margin-top: 16px;"
        />
      </div>
      
      <template #footer>
        <el-button @click="showCustomerServiceDialog = false">取消</el-button>
        <el-button type="primary" @click="startChatWithCustomerService">
          开始咨询
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '../../stores/user';
import { useChatStore } from '../../stores/chat';
import ChatList from '../../components/chat/ChatList.vue';
import ChatWindow from '../../components/chat/ChatWindow.vue';
import { ArrowDown, Search } from '@element-plus/icons-vue';
import axios from '../../utils/axios';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const chatStore = useChatStore();

// 状态
const activeConversationId = ref<number | null>(null);
const loadingConversations = computed(() => chatStore.loadingConversations);

// 商家选择对话框
const showMerchantDialog = ref(false);
const merchantSearchKeyword = ref('');
const merchants = ref<any[]>([]);
const filteredMerchants = ref<any[]>([]);
const loadingMerchants = ref(false);

// 客服对话框
const showCustomerServiceDialog = ref(false);
const customerServiceType = ref('product');
const customerServiceMessage = ref('');

// 下拉菜单命令处理
const handleCommand = (command: string) => {
  if (command === 'showMerchantList') {
    showMerchantDialog.value = true;
    loadMerchants();
  } else if (command === 'showCustomerService') {
    showCustomerServiceDialog.value = true;
  }
};

// 加载商家列表
const loadMerchants = async () => {
  loadingMerchants.value = true;
  
  try {
    // 尝试从商家API获取列表
    const response = await axios.get('/api/merchants');
    
    // 如果获取成功并且是数组
    if (response.data && Array.isArray(response.data)) {
      merchants.value = response.data.map(merchant => ({
        ...merchant,
        name: merchant.name || merchant.username || '未知商家'
      }));
    } else {
      // 如果获取失败或不是数组，尝试从用户API获取商家角色的用户
      console.warn('从商家API获取商家列表失败，尝试从用户API获取');
      const userResponse = await axios.get('/api/users', { 
        params: { role: 'MERCHANT' } 
      });
      
      if (userResponse.data && Array.isArray(userResponse.data)) {
        merchants.value = userResponse.data.map(user => ({
          id: user.id,
          name: user.realName || user.nickname || user.username || '未知商家',
          username: user.username
        }));
      } else {
        // 如果两种方式都失败，使用兜底的模拟数据
        console.warn('无法获取商家列表，使用兜底数据');
        merchants.value = [
          { id: 1, name: '系统客服' },
          { id: 2, name: '华为官方商城' },
          { id: 3, name: '小米自营店' }
        ];
      }
    }
    
    // 设置过滤后的数据
    filteredMerchants.value = merchants.value;
  } catch (error) {
    console.error('获取商家列表失败:', error);
    // 使用兜底数据
    merchants.value = [
      { id: 1, name: '系统客服' },
      { id: 2, name: '华为官方商城' },
      { id: 3, name: '小米自营店' }
    ];
    filteredMerchants.value = merchants.value;
  } finally {
    loadingMerchants.value = false;
  }
};

// 搜索商家
const handleMerchantSearch = () => {
  const keyword = merchantSearchKeyword.value.toLowerCase();
  
  if (!keyword) {
    filteredMerchants.value = merchants.value;
    return;
  }
  
  filteredMerchants.value = merchants.value.filter(merchant => 
    (merchant.name && merchant.name.toLowerCase().includes(keyword))
  );
};

// 开始与商家聊天
const startChatWithMerchant = async (merchant: any) => {
  if (!merchant || !merchant.id) {
    console.error('Merchant data incomplete, cannot start chat:', merchant);
    return;
  }
  
  try {
    const conversation = await chatStore.getOrCreateConversationWithUser(merchant.id);
    activeConversationId.value = conversation.id;
    showMerchantDialog.value = false;
  } catch (error) {
    console.error('Failed to start chat:', error);
    ElMessage.error('无法开始聊天，请稍后重试');
  }
};

// 开始与客服聊天
const startChatWithCustomerService = async () => {
  try {
    // 假设客服ID为1或从配置中获取
    const customerServiceId = 1; // 可以从环境变量或配置中获取
    
    // 如果有初始消息，设置到store中
    if (customerServiceMessage.value) {
      const typeText = {
        product: '商品咨询',
        order: '订单问题',
        refund: '退款售后',
        other: '其他问题'
      }[customerServiceType.value];
      
      const initialMessage = `[${typeText}] ${customerServiceMessage.value}`;
      chatStore.setInitialMessage(initialMessage);
    }
    
    const conversation = await chatStore.getOrCreateConversationWithUser(customerServiceId);
    activeConversationId.value = conversation.id;
    showCustomerServiceDialog.value = false;
  } catch (error) {
    console.error('Failed to start chat with customer service:', error);
    ElMessage.error('无法连接客服，请稍后重试');
  }
};

// 选择会话
const handleSelectConversation = (conversationId: number) => {
  activeConversationId.value = conversationId;
  // 同时存储到localStorage，用于通知服务判断
  localStorage.setItem('activeConversationId', conversationId.toString());
};

// 发送消息后处理
const handleMessageSent = () => {
  // 这里可以添加任何消息发送后的逻辑
};

// 初始化
onMounted(async () => {
  // 初始化聊天服务
  await chatStore.initChat();
  
  // 加载会话列表
  await chatStore.loadConversations();
  
  // 检查URL参数中是否有指定会话
  const urlParams = new URLSearchParams(window.location.search);
  const conversationIdParam = urlParams.get('conversation');
  
  if (conversationIdParam) {
    // 如果URL参数中有会话ID，优先选择该会话
    const conversationId = parseInt(conversationIdParam);
    if (!isNaN(conversationId)) {
      activeConversationId.value = conversationId;
      // 同时存储到localStorage，用于通知服务判断
      localStorage.setItem('activeConversationId', conversationId.toString());
    }
  } else if (chatStore.conversations.length > 0) {
    // 如果没有指定会话，选择第一个
    activeConversationId.value = chatStore.conversations[0].id;
    // 同时存储到localStorage，用于通知服务判断
    localStorage.setItem('activeConversationId', chatStore.conversations[0].id.toString());
  }
  
  // 监听通知点击事件，切换会话
  window.addEventListener('switchConversation', ((event: CustomEvent) => {
    if (event.detail && event.detail.conversationId) {
      handleSelectConversation(event.detail.conversationId);
    }
  }) as EventListener);
});
</script>

<style scoped>
.chat-page {
  height: 100vh;
  max-height: 100vh;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.chat-container {
  display: flex;
  height: 100%;
  max-height: 100vh;
  overflow: hidden !important;
  flex: 1;
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.chat-main {
  flex: 1;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.customer-service-info {
  padding: 16px 0;
}
</style> 