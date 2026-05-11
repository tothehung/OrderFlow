import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { format } from 'date-fns'
import { RefreshCw, Eye } from 'lucide-react'
import { orderApi } from '@/api/orderApi'
import { StatusBadge } from '@/components/common/StatusBadge'
import { useOrdersBroadcast } from '@/hooks/useOrderWebSocket'
import type { OrderStatus } from '@/types/order'
import toast from 'react-hot-toast'

const STATUS_OPTIONS: { label: string; value: OrderStatus | '' }[] = [
  { label: 'All statuses', value: '' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'Processing', value: 'PROCESSING' },
  { label: 'Shipped', value: 'SHIPPED' },
  { label: 'Delivered', value: 'DELIVERED' },
  { label: 'Cancelled', value: 'CANCELLED' },
]

export function OrderList() {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>('')

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['orders', page, statusFilter],
    queryFn: () => orderApi.list({
      page,
      size: 20,
      status: statusFilter || undefined,
    }),
  })

  // Real-time: invalidate list when any order changes
  useOrdersBroadcast((msg) => {
    if (['ORDER_CREATED', 'ORDER_STATUS_UPDATED', 'ORDER_CANCELLED'].includes(msg.type)) {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      queryClient.invalidateQueries({ queryKey: ['order-stats'] })
      toast.success(`Order ${msg.orderId.slice(-6)} updated`, { duration: 2000 })
    }
  })

  const orders = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  return (
    <div>
      {/* Toolbar */}
      <div className="flex items-center justify-between mb-4">
        <select
          className="text-sm border border-gray-200 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-brand-500"
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value as OrderStatus | ''); setPage(0) }}
        >
          {STATUS_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>

        <button
          onClick={() => queryClient.invalidateQueries({ queryKey: ['orders'] })}
          className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700 transition-colors"
        >
          <RefreshCw className={`w-4 h-4 ${isFetching ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Table */}
      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-500 text-xs uppercase tracking-wider">
            <tr>
              <th className="px-4 py-3 text-left">Order ID</th>
              <th className="px-4 py-3 text-left">Customer</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-right">Amount</th>
              <th className="px-4 py-3 text-left">Created</th>
              <th className="px-4 py-3 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50 bg-white">
            {isLoading && (
              <tr><td colSpan={6} className="py-12 text-center text-gray-400">Loading orders...</td></tr>
            )}
            {!isLoading && orders.length === 0 && (
              <tr><td colSpan={6} className="py-12 text-center text-gray-400">No orders found</td></tr>
            )}
            {orders.map((order) => (
              <tr key={order.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3">
                  <span className="font-mono text-xs text-gray-500">#{order.id.slice(-8).toUpperCase()}</span>
                </td>
                <td className="px-4 py-3">
                  <div className="font-medium text-gray-900">{order.customerName}</div>
                  <div className="text-xs text-gray-400">{order.customerEmail}</div>
                </td>
                <td className="px-4 py-3"><StatusBadge status={order.status} size="sm" /></td>
                <td className="px-4 py-3 text-right font-medium">
                  ${order.totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
                <td className="px-4 py-3 text-gray-500">
                  {format(new Date(order.createdAt), 'MMM d, HH:mm')}
                </td>
                <td className="px-4 py-3 text-center">
                  <Link
                    to={`/orders/${order.id}`}
                    className="inline-flex items-center gap-1 text-brand-600 hover:text-brand-700 font-medium"
                  >
                    <Eye className="w-4 h-4" /> View
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between mt-4 text-sm text-gray-500">
          <span>{data?.totalElements} orders total</span>
          <div className="flex gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
              className="px-3 py-1.5 border rounded-lg disabled:opacity-40 hover:bg-gray-50"
            >
              Previous
            </button>
            <span className="px-3 py-1.5">Page {page + 1} of {totalPages}</span>
            <button
              disabled={data?.last}
              onClick={() => setPage(p => p + 1)}
              className="px-3 py-1.5 border rounded-lg disabled:opacity-40 hover:bg-gray-50"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  )
}