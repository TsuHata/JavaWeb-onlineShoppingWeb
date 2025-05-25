import { useUserStore } from '../stores/user';

/**
 * 通知服务 - 管理浏览器通知
 */
class NotificationService {
  private enabled: boolean = false;
  private initialized: boolean = false;

  // 获取当前通知状态
  public getStatus(): { enabled: boolean, initialized: boolean, permission: string } {
    return {
      enabled: this.enabled,
      initialized: this.initialized,
      permission: Notification.permission
    };
  }

  // 重置通知服务状态，强制重新初始化
  public async reset(): Promise<boolean> {
    console.log('重置通知服务状态');
    this.enabled = false;
    this.initialized = false;
    return await this.init();
  }

  /**
   * 初始化通知服务
   * @returns 是否成功初始化
   */
  public async init(): Promise<boolean> {
    if (this.initialized) return this.enabled;

    // 检查浏览器是否支持通知
    if (!('Notification' in window)) {
      console.warn('该浏览器不支持通知功能');
      this.initialized = true;
      return false;
    }

    // 如果已经有权限，则启用通知
    if (Notification.permission === 'granted') {
      this.enabled = true;
      this.initialized = true;
      return true;
    } 
    
    // 如果已经被拒绝，则不能启用通知
    if (Notification.permission === 'denied') {
      console.warn('通知权限已被用户拒绝');
      this.initialized = true;
      return false;
    }

    // 请求权限
    try {
      const permission = await Notification.requestPermission();
      this.enabled = permission === 'granted';
      this.initialized = true;
      return this.enabled;
    } catch (error) {
      console.error('请求通知权限失败:', error);
      this.initialized = true;
      return false;
    }
  }

  /**
   * 发送通知
   * @param title 通知标题
   * @param options 通知选项
   * @returns 通知对象或null
   */
  public async sendNotification(title: string, options?: NotificationOptions): Promise<Notification | null> {
    if (!this.enabled && !await this.init()) {
      return null;
    }

    try {
      const notification = new Notification(title, {
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        ...options
      });

      // 点击通知时的处理
      notification.onclick = () => {
        window.focus();
        
        // 如果有会话ID，跳转到对应的聊天页面
        if (options?.data?.conversationId) {
          const conversationId = options.data.conversationId;
          console.log('通知点击，跳转到会话:', conversationId);
          
          // 检查当前是否在聊天页面，如果不是则跳转
          if (!window.location.pathname.includes('/chat')) {
            // 使用相对路径跳转
            window.location.href = `/chat?conversation=${conversationId}`;
          } else {
            // 如果已经在聊天页面，发送事件通知切换会话
            const event = new CustomEvent('switchConversation', { 
              detail: { conversationId }
            });
            window.dispatchEvent(event);
          }
        }
        
        notification.close();
      };

      return notification;
    } catch (error) {
      console.error('发送通知失败:', error);
      return null;
    }
  }

  /**
   * 发送聊天消息通知
   * @param sender 发送者名称
   * @param message 消息内容
   * @param conversationId 会话ID
   */
  public async sendChatNotification(sender: string, message: string, conversationId?: number): Promise<void> {
    console.log('准备发送聊天通知:', { sender, messagePreview: message.substring(0, 20), conversationId });
    
    // 获取当前用户角色
    const userStore = useUserStore();
    console.log('当前用户角色:', {
      isMerchant: userStore.isMerchant,
      roles: userStore.user.roles
    });
    
    // 移除商家角色限制，所有用户都可以收到通知
    // if (!isMerchant) {
    //   console.log('用户非商家身份，跳过通知');
    //   return;
    // }

    // 检查通知是否已启用
    if (!this.enabled) {
      console.log('通知服务未启用，尝试初始化...');
      const initialized = await this.init();
      console.log('通知服务初始化结果:', initialized);
      if (!initialized) {
        console.warn('通知服务初始化失败，无法发送通知');
        return;
      }
    }

    // 如果消息过长，截断显示
    const displayMessage = message.length > 50 ? `${message.substring(0, 50)}...` : message;

    // 发送通知
    try {
      const notification = await this.sendNotification(`来自 ${sender} 的新消息`, {
        body: displayMessage,
        tag: `chat_${conversationId || 'new'}`, // 使用tag避免同一会话的多条消息堆积
        data: { conversationId },
        requireInteraction: false, // 不需要用户交互，几秒后自动关闭
        silent: false // 通知时发出声音
      });
      
      console.log('通知发送结果:', notification ? '成功' : '失败');
      return;
    } catch (error) {
      console.error('发送聊天通知失败:', error);
    }
  }
}

export default new NotificationService(); 