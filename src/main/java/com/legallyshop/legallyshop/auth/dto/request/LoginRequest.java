package com.legallyshop.legallyshop.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được trống")
    private String email;

    @NotBlank(message = "Mật khẩu không được trống")
    private String password;

}
