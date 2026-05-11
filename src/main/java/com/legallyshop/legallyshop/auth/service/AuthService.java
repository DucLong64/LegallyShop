package com.legallyshop.legallyshop.auth.service;

import com.legallyshop.legallyshop.auth.JwtUtil;
import com.legallyshop.legallyshop.auth.dto.request.LoginRequest;
import com.legallyshop.legallyshop.auth.dto.request.RegisterRequest;
import com.legallyshop.legallyshop.auth.dto.request.RefreshTokenRequest;
import com.legallyshop.legallyshop.auth.dto.response.AuthResponse;
import com.legallyshop.legallyshop.common.exception.AppException;
import com.legallyshop.legallyshop.user.entity.User;
import com.legallyshop.legallyshop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new AppException(400, "Email đã được đăng ký");

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        userRepo.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException(401, "Sai email hoặc mật khẩu"));

        if (!encoder.matches(req.getPassword(), user.getPassword()))
            throw new AppException(401, "Sai email hoặc mật khẩu");

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwt.generateAccessToken(user);
        String refreshToken = jwt.generateRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken,
                user.getId(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse refreshToken(RefreshTokenRequest req) {
        String token = req.getRefreshToken();
        if (token == null || jwt.parseToken(token).getExpiration().before(new java.util.Date())) {
            throw new AppException(401, "Refresh token không hợp lệ hoặc đã hết hạn");
        }

        Long userId = Long.valueOf(jwt.parseToken(token).getSubject());
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(404, "Người dùng không tồn tại"));

        return buildAuthResponse(user);
    }
}