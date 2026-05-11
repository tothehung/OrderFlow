import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

// Import các trang mới tạo
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { PageTransition } from './components/common/PageTransition';
// Import các component của Dashboard cũ
import { CreateOrderForm } from './components/order/CreateOrderForm';
import { OrderList } from './components/order/OrderList';
import { StatsCards } from './components/common/StatsCards';

// Tạo một Component giả lập cho Dashboard
const DashboardPage = () => (
  <div className="min-h-screen bg-slate-50 text-slate-800">
    <header className="bg-white border-b border-slate-100 sticky top-0 z-10 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 py-4 sm:px-6 lg:px-8 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <span className="p-2 bg-brand-600 text-white rounded-lg font-bold text-lg">OF</span>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-slate-900">OrderFlow Platform</h1>
          </div>
        </div>
        <button onClick={() => window.location.href='/login'} className="text-sm font-medium text-red-600 hover:text-red-700">
          Đăng xuất
        </button>
      </div>
    </header>
    <main className="max-w-7xl mx-auto px-4 py-6 sm:px-6 lg:px-8 space-y-6">
      <StatsCards />
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-5 bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-900 mb-4">Tạo đơn hàng</h2>
          <CreateOrderForm />
        </div>
        <div className="lg:col-span-7 bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-900 mb-4">Bảng giám sát đơn hàng (Real-time)</h2>
          <OrderList />
        </div>
      </div>
    </main>
  </div>
);

const App: React.FC = () => {
  return (
    <>
      <Toaster position="top-right" />
      <Routes>
        {/* Mặc định vào thẳng Login */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        
        {/* Các trang Auth */}
        <Route path="/login" element={<PageTransition><Login /></PageTransition>} />
        <Route path="/register" element={<PageTransition><Register /></PageTransition>} />
        
        {/* Trang Dashboard */}
        <Route path="/dashboard" element={<DashboardPage />} />
      </Routes>
    </>
  );
};

export default App;