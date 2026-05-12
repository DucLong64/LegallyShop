package com.legallyshop.legallyshop.user.controller;

import com.legallyshop.legallyshop.common.response.ApiResponse;
import com.legallyshop.legallyshop.user.dto.request.UpdateProfileRequest;
import com.legallyshop.legallyshop.user.dto.response.UserProfileResponse;
import com.legallyshop.legallyshop.user.entity.User;
import com.legallyshop.legallyshop.user.entity.UserPrincipal;
import com.legallyshop.legallyshop.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Quản lý tài khoản")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(user.getId(), req));
    }
}

