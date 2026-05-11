import { useFieldArray, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Plus, Trash2 } from 'lucide-react'
import { orderApi } from '@/api/orderApi'
import toast from 'react-hot-toast'

const schema = z.object({
  customerId: z.string().min(1, 'Required'),
  customerName: z.string().min(2, 'Min 2 characters'),
  customerEmail: z.string().email('Valid email required'),
  items: z.array(z.object({
    productId: z.string().min(1, 'Required'),
    productName: z.string().min(1, 'Required'),
    quantity: z.number().int().min(1, 'Min 1').max(999),
    unitPrice: z.number().min(0.01, 'Must be positive'),
  })).min(1, 'Add at least one item'),
  shippingAddress: z.object({
    street: z.string().min(1, 'Required'),
    city: z.string().min(1, 'Required'),
    province: z.string().min(1, 'Required'),
    postalCode: z.string().min(1, 'Required'),
    country: z.string().min(1, 'Required'),
  }),
  notes: z.string().max(500).optional(),
})

type FormData = z.infer<typeof schema>

export function CreateOrderForm() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { register, control, handleSubmit, watch, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      customerId: 'CUST-001',
      items: [{ productId: '', productName: '', quantity: 1, unitPrice: 0 }],
      shippingAddress: { country: 'Vietnam' },
    },
  })

  const { fields, append, remove } = useFieldArray({ control, name: 'items' })
  const items = watch('items')
  const total = items.reduce((sum, i) => sum + (i.quantity || 0) * (i.unitPrice || 0), 0)

  const mutation = useMutation({
    mutationFn: orderApi.create,
    onSuccess: (order) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      toast.success('Order created!')
      navigate(`/orders/${order.id}`)
    },
  })

  const onSubmit = (data: FormData) => mutation.mutate(data)

  const inputClass = "w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
  const errorClass = "text-xs text-red-500 mt-1"
  const labelClass = "block text-sm font-medium text-gray-700 mb-1"

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 max-w-2xl">
      {/* Customer */}
      <section className="bg-white rounded-xl border border-gray-100 p-5">
        <h2 className="font-medium text-gray-900 mb-4">Customer info</h2>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className={labelClass}>Customer ID</label>
            <input className={inputClass} {...register('customerId')} />
            {errors.customerId && <p className={errorClass}>{errors.customerId.message}</p>}
          </div>
          <div>
            <label className={labelClass}>Full name</label>
            <input className={inputClass} {...register('customerName')} />
            {errors.customerName && <p className={errorClass}>{errors.customerName.message}</p>}
          </div>
          <div className="col-span-2">
            <label className={labelClass}>Email</label>
            <input className={inputClass} type="email" {...register('customerEmail')} />
            {errors.customerEmail && <p className={errorClass}>{errors.customerEmail.message}</p>}
          </div>
        </div>
      </section>

      {/* Items */}
      <section className="bg-white rounded-xl border border-gray-100 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-medium text-gray-900">Order items</h2>
          <button
            type="button"
            onClick={() => append({ productId: '', productName: '', quantity: 1, unitPrice: 0 })}
            className="flex items-center gap-1 text-sm text-brand-600 hover:text-brand-700 font-medium"
          >
            <Plus className="w-4 h-4" /> Add item
          </button>
        </div>

        <div className="space-y-3">
          {fields.map((field, i) => (
            <div key={field.id} className="grid grid-cols-12 gap-2 items-start">
              <div className="col-span-3">
                <input placeholder="Product ID" className={inputClass} {...register(`items.${i}.productId`)} />
              </div>
              <div className="col-span-4">
                <input placeholder="Product name" className={inputClass} {...register(`items.${i}.productName`)} />
              </div>
              <div className="col-span-2">
                <input placeholder="Qty" type="number" className={inputClass}
                  {...register(`items.${i}.quantity`, { valueAsNumber: true })} />
              </div>
              <div className="col-span-2">
                <input placeholder="Price" type="number" step="0.01" className={inputClass}
                  {...register(`items.${i}.unitPrice`, { valueAsNumber: true })} />
              </div>
              <div className="col-span-1 pt-2">
                {fields.length > 1 && (
                  <button type="button" onClick={() => remove(i)}
                    className="text-gray-400 hover:text-red-500 transition-colors">
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>

        <div className="mt-4 pt-3 border-t border-gray-100 flex justify-end">
          <span className="text-sm font-medium text-gray-900">
            Total: ${total.toFixed(2)}
          </span>
        </div>
      </section>

      {/* Shipping */}
      <section className="bg-white rounded-xl border border-gray-100 p-5">
        <h2 className="font-medium text-gray-900 mb-4">Shipping address</h2>
        <div className="grid grid-cols-2 gap-4">
          <div className="col-span-2">
            <label className={labelClass}>Street</label>
            <input className={inputClass} {...register('shippingAddress.street')} />
            {errors.shippingAddress?.street && <p className={errorClass}>{errors.shippingAddress.street.message}</p>}
          </div>
          {(['city', 'province', 'postalCode', 'country'] as const).map((field) => (
            <div key={field}>
              <label className={labelClass}>{field.charAt(0).toUpperCase() + field.slice(1)}</label>
              <input className={inputClass} {...register(`shippingAddress.${field}`)} />
            </div>
          ))}
        </div>
      </section>

      {/* Notes */}
      <section className="bg-white rounded-xl border border-gray-100 p-5">
        <label className={labelClass}>Notes (optional)</label>
        <textarea className={`${inputClass} resize-none`} rows={3} {...register('notes')} />
      </section>

      <button
        type="submit"
        disabled={mutation.isPending}
        className="w-full bg-brand-600 text-white py-2.5 rounded-xl font-medium hover:bg-brand-700 disabled:opacity-50 transition-colors"
      >
        {mutation.isPending ? 'Creating order...' : 'Create order'}
      </button>
    </form>
  )
}