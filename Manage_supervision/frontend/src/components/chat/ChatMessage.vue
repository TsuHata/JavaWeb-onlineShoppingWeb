<template>
  <div :class="['message-container', isCurrentUser ? 'sent' : 'received']">
    <!-- 接收方消息布局 -->
    <template v-if="!isCurrentUser">
      <div class="avatar">
        <el-avatar :size="40" :src="message.senderAvatar || defaultAvatar" />
      </div>
      <div class="message-content">
        <div class="message-sender" v-if="showSender">{{ message.senderName }}</div>
        
        <!-- 文件消息 -->
        <div v-if="isFileMessage" class="message-bubble file-message">
          <div class="file-preview">
            <!-- 图片文件预览 -->
            <div v-if="isImageFile" class="image-preview">
              <img :src="message.fileUrl" @click="previewImage" alt="图片" />
            </div>
            
            <!-- 其他文件类型图标 -->
            <div v-else class="file-icon" :class="fileIconClass">
              <i :class="fileIconName"></i>
            </div>
          </div>
          
          <div class="file-info">
            <div class="file-name">{{ message.fileName }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(message.fileSize || 0) }}</span>
              <a :href="message.fileUrl" target="_blank" download class="download-link">
                <el-button size="small" type="primary">下载</el-button>
              </a>
            </div>
            <div v-if="message.content" class="file-caption">{{ message.content }}</div>
          </div>
          
          <span class="message-time">{{ formattedTime }}</span>
        </div>
        
        <!-- 普通文本消息 -->
        <div v-else class="message-bubble">
          <span v-if="!processMessageContent.hasOrder && !processMessageContent.hasProduct" class="message-text">
            {{ message.content }}
          </span>
          <span v-else class="message-text">
            <template v-for="(part, index) in parsedContent" :key="index">
              <span v-if="part.type === 'text'">{{ part.text }}</span>
              <span 
                v-else-if="part.type === 'order'" 
                class="order-id" 
                @click="handleOrderClick(part.text)"
              >
                {{ part.text }}
              </span>
              <span 
                v-else-if="part.type === 'product'" 
                class="product-id" 
                @click="handleProductClick(part.productId)"
              >
                {{ part.text }}
              </span>
            </template>
          </span>
          <span class="message-time">{{ formattedTime }}</span>
        </div>
      </div>
    </template>
    
    <!-- 发送方消息布局 -->
    <template v-else>
      <div class="message-content">
        <!-- 文件消息 -->
        <div v-if="isFileMessage" class="message-bubble file-message">
          <div class="file-preview">
            <!-- 图片文件预览 -->
            <div v-if="isImageFile" class="image-preview">
              <img :src="message.fileUrl" @click="previewImage" alt="图片" />
            </div>
            
            <!-- 其他文件类型图标 -->
            <div v-else class="file-icon" :class="fileIconClass">
              <i :class="fileIconName"></i>
            </div>
          </div>
          
          <div class="file-info">
            <div class="file-name">{{ message.fileName }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(message.fileSize || 0) }}</span>
              <a :href="message.fileUrl" target="_blank" download class="download-link">
                <el-button size="small" type="primary">下载</el-button>
              </a>
            </div>
            <div v-if="message.content" class="file-caption">{{ message.content }}</div>
          </div>
          
          <span class="message-time">{{ formattedTime }}</span>
        </div>
        
        <!-- 普通文本消息 -->
        <div v-else class="message-bubble">
          <span v-if="!processMessageContent.hasOrder && !processMessageContent.hasProduct" class="message-text">
            {{ message.content }}
          </span>
          <span v-else class="message-text">
            <template v-for="(part, index) in parsedContent" :key="index">
              <span v-if="part.type === 'text'">{{ part.text }}</span>
              <span 
                v-else-if="part.type === 'order'" 
                class="order-id" 
                @click="handleOrderClick(part.text)"
              >
                {{ part.text }}
              </span>
              <span 
                v-else-if="part.type === 'product'" 
                class="product-id" 
                @click="handleProductClick(part.productId)"
              >
                {{ part.text }}
              </span>
            </template>
          </span>
          <span class="message-time">{{ formattedTime }}</span>
        </div>
        
        <div class="message-status">
          <el-icon v-if="message.isRead" class="read-icon"><Check /></el-icon>
        </div>
      </div>
      <div class="avatar">
        <el-avatar :size="40" :src="message.senderAvatar || defaultAvatar" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useUserStore } from '../../stores/user';
import { Check } from '@element-plus/icons-vue';
import type { ChatMessage } from '../../services/chat';
import { format } from 'date-fns';
import { ElImageViewer } from 'element-plus';
import { useRouter } from 'vue-router';

const props = defineProps<{
  message: ChatMessage;
  showSender?: boolean;
}>();

const userStore = useUserStore();
const router = useRouter();
const defaultAvatar = ref('/images/default-avatar.png');
const imageViewerVisible = ref(false);

// 判断消息是否是当前用户发送的
const isCurrentUser = computed(() => {
  return props.message.senderId === userStore.userId;
});

// 检查是否是文件消息
const isFileMessage = computed(() => {
  return !!props.message.fileUrl;
});

// 检查是否是图片文件
const isImageFile = computed(() => {
  if (!props.message.fileType) return false;
  
  return props.message.fileType.startsWith('image/');
});

// 获取文件图标的样式类
const fileIconClass = computed(() => {
  if (!props.message.fileType) return 'file-default';
  
  if (props.message.fileType.includes('pdf')) {
    return 'file-pdf';
  } else if (props.message.fileType.includes('word') || props.message.fileType.includes('document')) {
    return 'file-word';
  } else if (props.message.fileType.includes('excel') || props.message.fileType.includes('spreadsheet')) {
    return 'file-excel';
  } else if (props.message.fileType.includes('zip') || props.message.fileType.includes('rar') || props.message.fileType.includes('compressed')) {
    return 'file-zip';
  } else if (props.message.fileType.includes('audio')) {
    return 'file-audio';
  } else if (props.message.fileType.includes('video')) {
    return 'file-video';
  } else if (props.message.fileType.includes('text')) {
    return 'file-text';
  } else {
    return 'file-default';
  }
});

// 获取文件图标名称
const fileIconName = computed(() => {
  if (!props.message.fileType) return 'el-icon-document';
  
  if (props.message.fileType.includes('pdf')) {
    return 'el-icon-document-checked';
  } else if (props.message.fileType.includes('word') || props.message.fileType.includes('document')) {
    return 'el-icon-document';
  } else if (props.message.fileType.includes('excel') || props.message.fileType.includes('spreadsheet')) {
    return 'el-icon-document';
  } else if (props.message.fileType.includes('zip') || props.message.fileType.includes('rar') || props.message.fileType.includes('compressed')) {
    return 'el-icon-folder';
  } else if (props.message.fileType.includes('audio')) {
    return 'el-icon-headset';
  } else if (props.message.fileType.includes('video')) {
    return 'el-icon-video-camera';
  } else if (props.message.fileType.includes('text')) {
    return 'el-icon-document-copy';
  } else {
    return 'el-icon-document';
  }
});

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (!bytes) return '未知大小';
  
  if (bytes < 1024) {
    return bytes + ' B';
  } else if (bytes < 1024 * 1024) {
    return (bytes / 1024).toFixed(1) + ' KB';
  } else if (bytes < 1024 * 1024 * 1024) {
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  } else {
    return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
  }
};

// 预览图片
const previewImage = () => {
  if (isImageFile.value && props.message.fileUrl) {
    // 这里可以实现图片预览逻辑，例如使用 element-plus 的 ImageViewer 组件
    // 或者打开新窗口显示图片
    window.open(props.message.fileUrl, '_blank');
  }
};

// 检查是否包含订单号或商品ID
const processMessageContent = computed(() => {
  if (!props.message.content) return { text: '', hasOrder: false, hasProduct: false };
  
  let content = props.message.content;
  let hasOrder = false;
  let hasProduct = false;
  let orderMatch = null;
  let productMatch = null;
  
  // 检测订单号的正则表达式 - 例如: 2025051917302082f26a
  const orderRegex = /\b(\d{10,}[a-zA-Z0-9]{5,})\b/g;
  // 这里可以根据实际需求调整商品ID的正则表达式
  const productRegex = /\bproduct_(\d+)\b/g;
  
  // 查找订单号
  if (orderRegex.test(content)) {
    hasOrder = true;
    // 重置正则索引
    orderRegex.lastIndex = 0;
    orderMatch = content.match(orderRegex);
  }
  
  // 查找商品ID
  if (productRegex.test(content)) {
    hasProduct = true;
    // 重置正则索引
    productRegex.lastIndex = 0;
    productMatch = content.match(productRegex);
  }
  
  return { 
    text: content, 
    hasOrder, 
    hasProduct,
    orderMatch,
    productMatch
  };
});

// 解析消息内容，将订单号和商品ID替换为可点击的组件
const parsedContent = computed(() => {
  if (!props.message.content) return [];
  
  const content = props.message.content;
  const parts = [];
  let lastIndex = 0;
  
  // 订单号正则
  const orderRegex = /\b(\d{10,}[a-zA-Z0-9]{5,})\b/g;
  // 商品ID正则
  const productRegex = /\bproduct_(\d+)\b/g;
  
  // 合并所有匹配项并排序
  const matches = [];
  let match;
  
  // 查找所有订单号
  while ((match = orderRegex.exec(content)) !== null) {
    matches.push({
      start: match.index,
      end: match.index + match[0].length,
      text: match[0],
      type: 'order'
    });
  }
  
  // 查找所有商品ID
  while ((match = productRegex.exec(content)) !== null) {
    matches.push({
      start: match.index,
      end: match.index + match[0].length,
      text: match[0],
      type: 'product',
      productId: match[1]
    });
  }
  
  // 按开始位置排序
  matches.sort((a, b) => a.start - b.start);
  
  // 构建结果数组
  for (const match of matches) {
    // 添加匹配前的文本
    if (match.start > lastIndex) {
      parts.push({
        type: 'text',
        text: content.substring(lastIndex, match.start)
      });
    }
    
    // 添加匹配的订单号或商品ID
    parts.push({
      type: match.type,
      text: match.text,
      ...(match.type === 'product' ? { productId: match.productId } : {})
    });
    
    lastIndex = match.end;
  }
  
  // 添加最后一部分文本
  if (lastIndex < content.length) {
    parts.push({
      type: 'text',
      text: content.substring(lastIndex)
    });
  }
  
  return parts;
});

// 处理订单点击
const handleOrderClick = (orderNumber) => {
  // 这里可以实现订单预览的功能
  console.log('查看订单:', orderNumber);
  // 可以根据实际需求跳转或打开弹窗
  // router.push({ path: `/order/detail/${orderNumber}` });
  
  // 创建订单预览事件，让父组件处理
  const event = new CustomEvent('orderPreview', {
    detail: { orderNumber }
  });
  document.dispatchEvent(event);
};

// 处理商品点击
const handleProductClick = (productId) => {
  // 这里可以实现商品预览的功能
  console.log('查看商品:', productId);
  // 可以根据实际需求跳转或打开弹窗
  // router.push({ path: `/product/${productId}` });
  
  // 创建商品预览事件，让父组件处理
  const event = new CustomEvent('productPreview', {
    detail: { productId }
  });
  document.dispatchEvent(event);
};

// 格式化消息时间
const formattedTime = computed(() => {
  const date = new Date(props.message.sentTime);
  const now = new Date();
  const messageDate = new Date(date);
  
  // 如果是今天发送的消息，只显示时间
  if (
    messageDate.getDate() === now.getDate() &&
    messageDate.getMonth() === now.getMonth() &&
    messageDate.getFullYear() === now.getFullYear()
  ) {
    return format(date, 'HH:mm');
  }
  
  // 如果是昨天发送的
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (
    messageDate.getDate() === yesterday.getDate() &&
    messageDate.getMonth() === yesterday.getMonth() &&
    messageDate.getFullYear() === yesterday.getFullYear()
  ) {
    return '昨天 ' + format(date, 'HH:mm');
  }
  
  // 如果是今年发送的
  if (messageDate.getFullYear() === now.getFullYear()) {
    return format(date, 'MM-dd HH:mm');
  }
  
  // 其他情况显示完整日期
  return format(date, 'yyyy-MM-dd HH:mm');
});
</script>

<style scoped>
.message-container {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-container.sent {
  justify-content: flex-end;
}

.avatar {
  margin: 0 8px;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 18px;
  position: relative;
  word-break: break-word;
  display: inline-block;
}

.sent .message-bubble {
  background-color: #95ec69;
  margin-right: 8px;
  border-top-right-radius: 4px;
}

.received .message-bubble {
  background-color: #f0f0f0;
  margin-left: 8px;
  border-top-left-radius: 4px;
}

.message-text {
  font-size: 14px;
  line-height: 1.4;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-left: 8px;
  white-space: nowrap;
}

.message-status {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  text-align: right;
}

.read-icon {
  color: #409EFF;
  font-size: 14px;
}

/* 文件消息样式 */
.file-message {
  min-width: 220px;
  display: flex;
  flex-direction: column;
}

.file-preview {
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
}

.image-preview {
  max-width: 200px;
  max-height: 200px;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.file-icon i {
  font-size: 36px;
  color: #606266;
}

.file-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.file-name {
  margin-bottom: 4px;
  font-weight: 500;
  word-break: break-all;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.download-link {
  text-decoration: none;
}

.file-caption {
  margin-top: 8px;
  font-size: 14px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

/* 文件类型特定样式 */
.file-pdf i {
  color: #F56C6C;
}

.file-word i {
  color: #409EFF;
}

.file-excel i {
  color: #67C23A;
}

.file-zip i {
  color: #E6A23C;
}

.file-audio i {
  color: #409EFF;
}

.file-video i {
  color: #F56C6C;
}

.file-text i {
  color: #909399;
}

/* 订单号和商品ID样式 */
.order-id, .product-id {
  color: #409EFF;
  cursor: pointer;
  text-decoration: underline;
  font-weight: 500;
}

.order-id:hover, .product-id:hover {
  color: #66b1ff;
}

/* 预览弹窗样式 */
.preview-dialog {
  max-width: 500px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
  margin-bottom: 16px;
}

.preview-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.preview-content {
  max-height: 400px;
  overflow-y: auto;
}

.preview-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style> 