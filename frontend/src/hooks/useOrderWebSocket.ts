import { useEffect, useRef, useCallback } from 'react'
import type { WsMessage } from '@/types/order'

type WsHandler = (message: WsMessage) => void

export function useOrderWebSocket(orderId: string | null, onMessage: WsHandler) {
  const wsRef = useRef<WebSocket | null>(null)
  const onMessageRef = useRef(onMessage)
  onMessageRef.current = onMessage

  const connect = useCallback(() => {
    if (!orderId) return

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    const ws = new WebSocket(`${protocol}://${window.location.host}/ws/orders`)
    wsRef.current = ws

    ws.onopen = () => {
      ws.send(JSON.stringify({ type: 'SUBSCRIBE', orderId }))
    }

    ws.onmessage = (e) => {
      try {
        const msg: WsMessage = JSON.parse(e.data)
        onMessageRef.current(msg)
      } catch {
        // ignore malformed messages
      }
    }

    ws.onclose = (e) => {
      // Auto-reconnect unless intentional close (code 1000)
      if (e.code !== 1000) {
        setTimeout(connect, 3000)
      }
    }

    ws.onerror = () => ws.close()
  }, [orderId])

  useEffect(() => {
    connect()
    return () => {
      wsRef.current?.close(1000)
    }
  }, [connect])
}

// Subscribe to all orders (dashboard)
export function useOrdersBroadcast(onMessage: WsHandler) {
  return useOrderWebSocket('*', onMessage)
}