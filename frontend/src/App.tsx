import React from 'react';
import { CreateOrderForm } from './components/order/CreateOrderForm';
import { OrderList } from './components/order/OrderList';
import { StatsCards } from './components/common/StatsCards';
import { Toaster } from 'react-hot-toast';

const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      {/* Toaster thông báo nổi góc màn hình */}
      <Toaster position="top-right" reverseOrder={false} />

      {/* Header */}
      <header className="bg-white border-b border-slate-100 sticky top-0 z-10 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <span className="p-2 bg-brand-600 text-white rounded-lg font-bold text-lg shadow-md shadow-blue-200">
              OF
            </span>
            <div>
              <h1 className="text-xl font-bold tracking-tight text-slate-900">OrderFlow Platform</h1>
              <p className="text-xs text-slate-500 font-medium">Hệ thống xử lý đơn hàng thời gian thực qua Kafka</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Live Connection
            </span>
          </div>
        </div>
      </header>

      {/* Main Content Dashboard */}
      <main className="max-w-7xl mx-auto px-4 py-6 sm:px-6 lg:px-8 space-y-6">
        
        {/* Thẻ thống kê */}
        <StatsCards />

        {/* Bố cục 2 cột để TEST luồng */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          
          {/* Cột trái: Form tạo đơn (Chiếm 5/12 cột) */}
          <div className="lg:col-span-5 bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
            <div className="mb-4">
              <h2 className="text-lg font-semibold text-slate-900">Tạo đơn hàng thử nghiệm</h2>
              <p className="text-xs text-slate-400 mt-0.5">
                Nhập thông tin để giả lập hành vi đặt hàng của khách hàng.
              </p>
            </div>
            <CreateOrderForm />
          </div>

          {/* Cột phải: Danh sách đơn hàng nhận tin nhắn real-time (Chiếm 7/12 cột) */}
          <div className="lg:col-span-7 bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
            <div className="mb-4">
              <h2 className="text-lg font-semibold text-slate-900 font-sans">Bảng giám sát đơn hàng (Real-time)</h2>
              <p className="text-xs text-slate-400 mt-0.5">
                Danh sách cập nhật tự động nhờ lắng nghe tín hiệu từ WebSocket & Kafka.
              </p>
            </div>
            <OrderList />
          </div>

        </div>
      </main>
    </div>
  );
};

export default App;