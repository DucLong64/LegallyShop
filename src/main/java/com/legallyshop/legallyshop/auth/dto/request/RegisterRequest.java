package com.legallyshop.legallyshop.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được trống")
    private String email;

    @NotBlank(message = "Mật khẩu không được trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    private String fullName;
    private String phone;
}
