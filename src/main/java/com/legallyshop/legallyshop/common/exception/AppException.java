package com.legallyshop.legallyshop.common.exception;

import lombok.Getter;

// Custom exception để ném ra từ Service layer
// GlobalExceptionHandler sẽ bắt và format về ApiResponse
@Getter
public class AppException extends RuntimeException {
    private final int    code;
    private final String message;

    public AppException(int code, String message) {
        super(message);
        this.code    = code;
        this.message = message;
    }
}

// Dùng trong service:
// throw new AppException(404, "Sản phẩm không tồn tại");