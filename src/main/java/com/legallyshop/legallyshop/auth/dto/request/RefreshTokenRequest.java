package com.legallyshop.legallyshop.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token không được trống")
    private String refreshToken;

}
