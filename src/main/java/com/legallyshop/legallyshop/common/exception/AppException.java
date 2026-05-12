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
    public static AppException notFound(String resource) {
        return new AppException(404, resource + " không tồn tại");
    }

    public static AppException badRequest(String message) {
        return new AppException(400, message);
    }

    public static AppException unauthorized(String message) {
        return new AppException(401, message);
    }

    public static AppException forbidden() {
        return new AppException(403, "Bạn không có quyền thực hiện hành động này");
    }
}

// Dùng trong service:
// throw new AppException(404, "Sản phẩm không tồn tại");