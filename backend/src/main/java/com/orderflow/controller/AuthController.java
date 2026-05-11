package com.orderflow.controller;

import com.orderflow.dto.response.ApiResponse;
import com.orderflow.model.User;
import com.orderflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody User user) {
        return ApiResponse.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody User loginReq) {
        boolean isSuccess = authService.login(loginReq.getEmail(), loginReq.getPassword());
        
        return isSuccess 
                ? ApiResponse.ok("Đăng nhập thành công", "JWT_TOKEN_WILL_BE_HERE") 
                : ApiResponse.error("Sai email hoặc mật khẩu");
    }
}