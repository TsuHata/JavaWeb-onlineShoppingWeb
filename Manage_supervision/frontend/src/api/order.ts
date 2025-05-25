import request from '../utils/request'

// 创建订单
export const createOrder = (data: any) => {
  return request({
    url: '/api/orders',
    method: 'post',
    data
  })
}

// 获取用户订单列表
export const getUserOrders = (page: number, size: number) => {
  return request({
    url: '/api/orders',
    method: 'get',
    params: { page, size }
  })
}

// 获取订单详情
export const getOrderDetail = (id: number) => {
  return request({
    url: `/api/orders/${id}`,
    method: 'get'
  })
}

// 用户 - 取消订单（仅限待支付状态）
export const cancelOrder = (id: number) => {
  return request({
    url: `/api/orders/${id}/cancel`,
    method: 'post'
  })
}

// 确认收货
export const confirmReceived = (id: number) => {
  return request({
    url: `/api/orders/${id}/confirm`,
    method: 'post'
  })
}

// 商家 - 获取订单列表
export const getMerchantOrders = (page: number, size: number) => {
  return request({
    url: '/api/merchant/orders',
    method: 'get',
    params: { page, size }
  })
}

// 商家 - 获取订单详情
export const getMerchantOrderDetail = (id: number) => {
  return request({
    url: `/api/merchant/orders/${id}`,
    method: 'get'
  })
}

// 商家 - 发货
export const shipOrder = (id: number) => {
  return request({
    url: `/api/merchant/orders/${id}/ship`,
    method: 'post'
  })
}

// 商家 - 退款
export const refundOrder = (id: number, reason: string) => {
  return request({
    url: `/api/merchant/orders/${id}/refund`,
    method: 'post',
    data: { reason }
  })
}

// 商家 - 取消订单（仅适用于待支付状态的订单）
export const merchantCancelOrder = (id: number, reason: string) => {
  return request({
    url: `/api/merchant/orders/${id}/cancel`,
    method: 'post',
    data: { reason }
  })
}

// 用户 - 申请售后
export const applyAfterSale = (id: number, reason: string) => {
  return request({
    url: `/api/orders/${id}/after-sale`,
    method: 'post',
    params: { reason }
  })
}

// 商家 - 获取售后订单列表
export const getMerchantAfterSaleOrders = (page: number, size: number) => {
  return request({
    url: '/api/merchant/orders/after-sale',
    method: 'get',
    params: { page, size }
  })
}

// 商家 - 处理售后申请
export const processAfterSale = (id: number, approved: boolean, reason: string) => {
  return request({
    url: `/api/merchant/orders/${id}/process-after-sale`,
    method: 'post',
    params: { approved, reason }
  })
}

 