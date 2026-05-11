import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, ArrowRight, ShieldCheck } from 'lucide-react';
import toast from 'react-hot-toast';
import { authApi } from '../api/authApi';

export function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');       
  const [password, setPassword] = useState('');
  const handleLogin = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    const response = await authApi.login({ email, password });
    if (response.success) {
      toast.success('Đăng nhập thành công!');
      navigate('/dashboard');
    } else {
      toast.error(response.message);
    }
  } catch (error) {
    toast.error('Có lỗi xảy ra khi kết nối với máy chủ');
  }
};

  return (
    <div className="min-h-screen flex w-full bg-white">
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 sm:p-12 lg:p-24">
        <div className="w-full max-w-md space-y-8">
          <div>
            <div className="w-12 h-12 bg-brand-600 rounded-xl flex items-center justify-center shadow-lg shadow-brand-200 mb-6">
              <ShieldCheck className="w-6 h-6 text-white" />
            </div>
            <h2 className="text-3xl font-bold tracking-tight text-slate-900">
              Chào mừng trở lại
            </h2>
            <p className="text-sm text-slate-500 mt-2">
              Vui lòng đăng nhập để quản lý hệ thống OrderFlow.
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleLogin} className="space-y-5 mt-8">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Email
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-xl text-sm placeholder-slate-400 focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-colors"
                  placeholder="admin@orderflow.com"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-sm font-medium text-slate-700">
                  Mật khẩu
                </label>
                <a
                  href="#"
                  className="text-xs font-medium text-brand-600 hover:text-brand-500"
                >
                  Quên mật khẩu?
                </a>
              </div>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-xl text-sm placeholder-slate-400 focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-colors"
                  placeholder="••••••••"
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full flex justify-center items-center gap-2 py-2.5 px-4 border border-transparent rounded-xl shadow-sm text-sm font-medium text-white bg-brand-600 hover:bg-brand-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500 transition-all duration-200 active:scale-[0.98]"
            >
              Đăng nhập <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Chuyển sang Đăng ký */}
          <p className="text-center text-sm text-slate-600">
            Chưa có tài khoản?{" "}
            <Link
              to="/register"
              className="font-semibold text-brand-600 hover:text-brand-500 transition-colors"
            >
              Đăng ký ngay
            </Link>
          </p>
        </div>
      </div>

      <div className="hidden lg:flex lg:w-1/2 relative bg-slate-50 items-center justify-center p-12 overflow-hidden">
        {/* Vòng tròn Blur tạo hiệu ứng Glassmorphism */}
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-brand-400/30 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob"></div>
        <div className="absolute top-1/3 right-1/4 w-96 h-96 bg-purple-400/30 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob animation-delay-2000"></div>
        <div className="absolute bottom-1/4 left-1/3 w-96 h-96 bg-blue-400/30 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob animation-delay-4000"></div>

        <div className="relative z-10 max-w-lg text-center backdrop-blur-sm bg-white/30 p-8 rounded-3xl border border-white/40 shadow-xl">
          <h3 className="text-2xl font-bold text-slate-800 mb-4">
            Hệ sinh thái Quản lý Đơn hàng
          </h3>
          <p className="text-slate-600 leading-relaxed">
            Nền tảng OrderFlow giúp bạn xử lý hàng ngàn đơn hàng mỗi giây với
            kiến trúc Microservices và luồng dữ liệu thời gian thực.
          </p>
        </div>
      </div>
    </div>
  );
}