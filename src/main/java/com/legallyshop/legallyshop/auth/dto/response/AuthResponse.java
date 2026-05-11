package com.legallyshop.legallyshop.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long   userId;
    private String email;
    private String role;

    public static AuthResponse of(String accessToken, String refreshToken,
                                  Long userId, String email, String role) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }
}