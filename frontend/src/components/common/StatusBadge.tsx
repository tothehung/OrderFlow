import { clsx } from 'clsx'
import type { OrderStatus } from '@/types/order'

const STATUS_CONFIG: Record<OrderStatus, { label: string; className: string }> = {
  PENDING:    { label: 'Pending',    className: 'bg-amber-100 text-amber-800' },
  CONFIRMED:  { label: 'Confirmed',  className: 'bg-blue-100 text-blue-800' },
  PROCESSING: { label: 'Processing', className: 'bg-indigo-100 text-indigo-800' },
  SHIPPED:    { label: 'Shipped',    className: 'bg-violet-100 text-violet-800' },
  DELIVERED:  { label: 'Delivered',  className: 'bg-green-100 text-green-800' },
  CANCELLED:  { label: 'Cancelled',  className: 'bg-red-100 text-red-800' },
}

interface Props {
  status: OrderStatus
  size?: 'sm' | 'md'
}

export function StatusBadge({ status, size = 'md' }: Props) {
  const { label, className } = STATUS_CONFIG[status]
  return (
    <span className={clsx(
      'inline-flex items-center rounded-full font-medium',
      size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-sm',
      className
    )}>
      {label}
    </span>
  )
}