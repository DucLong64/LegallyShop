package com.legallyshop.legallyshop.common.response;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int code;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code,String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(201)
                .data(data)
                .build();
    }
}
