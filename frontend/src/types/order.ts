export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'

export interface OrderItem {
  productId: string
  productName: string
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface ShippingAddress {
  street: string
  city: string
  province: string
  postalCode: string
  country: string
}

export interface Order {
  id: string
  customerId: string
  customerName: string
  customerEmail: string
  items: OrderItem[]
  status: OrderStatus
  totalAmount: number
  shippingAddress: ShippingAddress
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface OrderStats {
  pending: number
  confirmed: number
  processing: number
  shipped: number
  delivered: number
  cancelled: number
  total: number
}

export interface CreateOrderPayload {
  customerId: string
  customerName: string
  customerEmail: string
  items: {
    productId: string
    productName: string
    quantity: number
    unitPrice: number
  }[]
  shippingAddress: ShippingAddress
  notes?: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
  timestamp: string
}

// WebSocket event types
export type WsEventType = 'ORDER_CREATED' | 'ORDER_STATUS_UPDATED' | 'ORDER_CANCELLED' | 'SUBSCRIBED'

export interface WsMessage {
  type: WsEventType
  orderId: string
  data: unknown
}