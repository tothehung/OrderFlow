import { useQuery } from '@tanstack/react-query'
import { orderApi } from '@/api/orderApi'
import type { OrderStats } from '@/types/order'

const STAT_CARDS: { key: keyof OrderStats; label: string; color: string }[] = [
  { key: 'total',      label: 'Total orders',  color: 'text-gray-900' },
  { key: 'pending',    label: 'Pending',        color: 'text-amber-600' },
  { key: 'confirmed',  label: 'Confirmed',      color: 'text-blue-600' },
  { key: 'processing', label: 'Processing',     color: 'text-indigo-600' },
  { key: 'shipped',    label: 'Shipped',        color: 'text-violet-600' },
  { key: 'delivered',  label: 'Delivered',      color: 'text-green-600' },
]

export function StatsCards() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['order-stats'],
    queryFn: orderApi.getStats,
    refetchInterval: 30_000,
  })

  if (isLoading) {
    return (
      <div className="grid grid-cols-3 md:grid-cols-6 gap-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="bg-gray-50 rounded-xl p-4 animate-pulse h-20" />
        ))}
      </div>
    )
  }

  return (
    <div className="grid grid-cols-3 md:grid-cols-6 gap-3">
      {STAT_CARDS.map(({ key, label, color }) => (
        <div key={key} className="bg-white rounded-xl border border-gray-100 p-4">
          <p className="text-xs text-gray-400 mb-1">{label}</p>
          <p className={`text-2xl font-semibold ${color}`}>{stats?.[key] ?? 0}</p>
        </div>
      ))}
    </div>
  )
}