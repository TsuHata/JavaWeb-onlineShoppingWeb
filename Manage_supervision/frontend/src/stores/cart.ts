import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

interface CartItem {
  id: number
  name: string
  price: number
  quantity: number
  imageUrl: string
  stock: number
  merchantId: number
  merchantName?: string
}

export const useCartStore = defineStore('cart', () => {
  // 购物车商品列表
  const cartItems = ref<CartItem[]>([])
  
  // 从localStorage加载购物车数据
  const initCart = () => {
    const savedCart = localStorage.getItem('cart')
    if (savedCart) {
      try {
        cartItems.value = JSON.parse(savedCart)
      } catch (error) {
        console.error('解析购物车数据失败', error)
        cartItems.value = []
      }
    }
  }
  
  // 监听购物车变化，保存到localStorage
  watch(
    cartItems,
    (items) => {
      localStorage.setItem('cart', JSON.stringify(items))
    },
    { deep: true }
  )
  
  // 计算购物车商品总数
  const totalItems = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.quantity, 0)
  })
  
  // 计算购物车商品总价
  const totalPrice = computed(() => {
    return cartItems.value.reduce((total, item) => {
      return total + (item.price * item.quantity)
    }, 0)
  })
  
  // 添加商品到购物车
  const addToCart = (product: any, quantity = 1) => {
    // 检查库存
    if (product.stock < quantity) {
      ElMessage.error('商品库存不足')
      return false
    }
    
    // 查找购物车中是否已有该商品
    const existingItem = cartItems.value.find(item => item.id === product.id)
    
    if (existingItem) {
      // 检查增加数量后是否超出库存
      if (existingItem.quantity + quantity > product.stock) {
        ElMessage.error('商品库存不足')
        return false
      }
      
      // 更新数量
      existingItem.quantity += quantity
      ElMessage.success('已更新购物车')
    } else {
      // 添加新商品
      cartItems.value.push({
        id: product.id,
        name: product.name,
        price: product.price,
        quantity: quantity,
        imageUrl: product.imageUrl,
        stock: product.stock,
        merchantId: product.merchantId,
        merchantName: product.merchantName
      })
      ElMessage.success('已加入购物车')
    }
    
    return true
  }
  
  // 从购物车移除商品
  const removeFromCart = (productId: number) => {
    const index = cartItems.value.findIndex(item => item.id === productId)
    if (index !== -1) {
      cartItems.value.splice(index, 1)
      ElMessage.success('已从购物车移除')
      return true
    }
    return false
  }
  
  // 更新购物车商品数量
  const updateQuantity = (productId: number, quantity: number) => {
    const item = cartItems.value.find(item => item.id === productId)
    if (item) {
      // 检查库存
      if (quantity > item.stock) {
        ElMessage.error('商品库存不足')
        return false
      }
      
      // 如果数量为0，移除商品
      if (quantity <= 0) {
        return removeFromCart(productId)
      }
      
      // 更新数量
      item.quantity = quantity
      ElMessage.success('已更新购物车')
      return true
    }
    return false
  }
  
  // 清空购物车
  const clearCart = () => {
    cartItems.value = []
    ElMessage.success('已清空购物车')
  }
  
  // 根据商家ID分组商品
  const cartItemsByMerchant = computed(() => {
    const grouped: Record<number, CartItem[]> = {}
    
    cartItems.value.forEach(item => {
      if (!grouped[item.merchantId]) {
        grouped[item.merchantId] = []
      }
      grouped[item.merchantId].push(item)
    })
    
    return grouped
  })
  
  // 初始化购物车
  initCart()
  
  return {
    cartItems,
    totalItems,
    totalPrice,
    cartItemsByMerchant,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart
  }
}) 