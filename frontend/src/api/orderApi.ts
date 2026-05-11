import type { ApiResponse, CreateOrderPayload, Order, OrderStats, OrderStatus, PageResponse } from '@/types/order'
import { apiClient } from './client'

export const orderApi = {
  create: async (payload: CreateOrderPayload): Promise<Order> => {
    const { data } = await apiClient.post<ApiResponse<Order>>('/orders', payload)
    return data.data
  },

  getById: async (id: string): Promise<Order> => {
    const { data } = await apiClient.get<ApiResponse<Order>>(`/orders/${id}`)
    return data.data
  },

  list: async (params?: {
    page?: number
    size?: number
    status?: OrderStatus
    customerId?: string
  }): Promise<PageResponse<Order>> => {
    const { data } = await apiClient.get<ApiResponse<PageResponse<Order>>>('/orders', { params })
    return data.data
  },

  updateStatus: async (id: string, status: OrderStatus, reason?: string): Promise<Order> => {
    const { data } = await apiClient.patch<ApiResponse<Order>>(`/orders/${id}/status`, { status, reason })
    return data.data
  },

  cancel: async (id: string, reason?: string): Promise<void> => {
    await apiClient.delete(`/orders/${id}`, { params: { reason } })
  },

  getStats: async (): Promise<OrderStats> => {
    const { data } = await apiClient.get<ApiResponse<OrderStats>>('/orders/stats')
    return data.data
  },
}