package com.legallyshop.legallyshop.auth.controller;

import com.legallyshop.legallyshop.auth.dto.request.LoginRequest;
import com.legallyshop.legallyshop.auth.dto.request.RefreshTokenRequest;
import com.legallyshop.legallyshop.auth.dto.request.RegisterRequest;
import com.legallyshop.legallyshop.auth.dto.response.AuthResponse;
import com.legallyshop.legallyshop.auth.service.AuthService;
import com.legallyshop.legallyshop.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(CREATED)
    @Operation(summary = "Đăng ký tài khoản mới")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req) {
        return ApiResponse.created(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập",
            description = "Nhận email và mật khẩu, trả về JWT. Access Token hết hạn sau 15 phút."
            )
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @RequestBody RefreshTokenRequest req) {
        return ApiResponse.ok(authService.refreshToken(req));
    }
}
