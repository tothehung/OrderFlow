import axios from 'axios'
import toast from 'react-hot-toast'

export const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
})

// Response interceptor — unwrap ApiResponse wrapper
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || 'Something went wrong'
    toast.error(message)
    return Promise.reject(error)
  }
)