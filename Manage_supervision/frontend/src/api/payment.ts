import request from '../utils/request'

// 创建支付
export const createPayment = (data: any) => {
  return request({
    url: '/api/payments',
    method: 'post',
    data
  })
}

// 完成支付（模拟支付成功回调）
export const completePayment = (id: number) => {
  return request({
    url: `/api/payments/${id}/complete`,
    method: 'post'
  })
}

// 取消支付
export const cancelPayment = (id: number) => {
  return request({
    url: `/api/payments/${id}/cancel`,
    method: 'post'
  })
}

// 申请退款
export const refundPayment = (data: any) => {
  return request({
    url: '/api/payments/refund',
    method: 'post',
    data
  })
}

// 获取用户支付记录
export const getUserPayments = () => {
  return request({
    url: '/api/payments',
    method: 'get'
  })
}

// 获取订单支付记录
export const getOrderPayments = (orderId: number) => {
  return request({
    url: `/api/payments/order/${orderId}`,
    method: 'get'
  })
}

// 获取支付详情
export const getPaymentDetail = (id: number) => {
  return request({
    url: `/api/payments/${id}`,
    method: 'get'
  })
} 