import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Phone, ArrowRight, ShieldCheck, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { authApi } from '../api/authApi';
import { PageTransition } from '../components/common/PageTransition';

export function Register() {
  const navigate = useNavigate();
  
  // 1. Khai báo State để quản lý dữ liệu người dùng nhập vào
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // 2. Hàm xử lý khi nhấn nút Đăng ký
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    try {
      // Gọi API gửi dữ liệu xuống Backend (MongoDB)
      const response = await authApi.register({ 
        fullName, 
        email, 
        phone,
        password 
      });

      if (response.success) {
        toast.success('Tạo tài khoản thành công! Vui lòng đăng nhập.');
        navigate('/login');
      } else {
        toast.error(response.message || 'Đăng ký thất bại');
      }
    } catch (error: any) {
      // Xử lý lỗi kết nối hoặc lỗi từ server
      const errorMsg = error.response?.data?.message || 'Không thể kết nối đến máy chủ';
      toast.error(errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <PageTransition>
      <div className="min-h-screen flex w-full bg-white">
        {/* Cột trái: Giới thiệu & Hiệu ứng (Chỉ hiện trên Desktop) */}
        <div className="hidden lg:flex lg:w-1/2 relative bg-brand-600 items-center justify-center p-12 overflow-hidden">
          <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-10"></div>
          <div className="relative z-10 max-w-lg text-center text-white">
            <div className="w-20 h-20 bg-white/10 backdrop-blur-lg rounded-2xl flex items-center justify-center mx-auto mb-8 border border-white/20">
              <ShieldCheck className="w-10 h-10 text-brand-200" />
            </div>
            <h3 className="text-4xl font-bold mb-6">Trở thành một phần của OrderFlow</h3>
            <p className="text-brand-100 text-lg leading-relaxed">
              Quản lý đơn hàng thời gian thực, tối ưu hóa kho bãi và tự động hóa quy trình thanh toán chỉ trong một nền tảng duy nhất.
            </p>
          </div>
          
          {/* Hiệu ứng trang trí phía sau */}
          <div className="absolute -bottom-24 -left-24 w-96 h-96 bg-brand-500 rounded-full mix-blend-multiply filter blur-3xl opacity-50"></div>
        </div>

        {/* Cột phải: Form Đăng ký */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-8 sm:p-12 lg:p-24">
          <div className="w-full max-w-md space-y-8">
            <div>
              <h2 className="text-3xl font-bold tracking-tight text-slate-900">Bắt đầu miễn phí</h2>
              <p className="text-sm text-slate-500 mt-2">Điền thông tin của bạn để tạo tài khoản quản trị.</p>
            </div>

            <form onSubmit={handleRegister} className="space-y-5">
              {/* Ô nhập Họ và Tên */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Họ và tên</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <User className="h-5 w-5 text-slate-400" />
                  </div>
                  <input 
                    type="text" 
                    required 
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-all" 
                    placeholder="Nguyễn Văn A" 
                  />
                </div>
              </div>

              {/* Ô nhập Email */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Email công việc</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Mail className="h-5 w-5 text-slate-400" />
                  </div>
                  <input 
                    type="email" 
                    required 
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-all" 
                    placeholder="name@company.com" 
                  />
                </div>
              </div>

              {/* Ô nhập Số điện thoại */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Số điện thoại</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Phone className="h-5 w-5 text-slate-400" />
                  </div>
                  <input 
                    type="tel" 
                    required 
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-all" 
                    placeholder="09xx xxx xxx" 
                  />
                </div>
              </div>

              {/* Ô nhập Mật khẩu */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Mật khẩu</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Lock className="h-5 w-5 text-slate-400" />
                  </div>
                  <input 
                    type="password" 
                    required 
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-all" 
                    placeholder="Tối thiểu 8 ký tự" 
                  />
                </div>
              </div>

              {/* Nút Submit */}
              <button 
                type="submit" 
                disabled={isLoading}
                className="w-full flex justify-center items-center gap-2 py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-slate-900 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-slate-900 transition-all duration-200 active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" /> Đang xử lý...
                  </>
                ) : (
                  <>
                    Tạo tài khoản ngay <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>

            <p className="text-center text-sm text-slate-600">
              Bạn đã có tài khoản?{' '}
              <Link to="/login" className="font-bold text-brand-600 hover:text-brand-500 transition-colors">
                Đăng nhập tại đây
              </Link>
            </p>
          </div>
        </div>
      </div>
    </PageTransition>
  );
}