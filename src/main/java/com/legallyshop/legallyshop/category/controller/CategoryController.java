package com.legallyshop.legallyshop.category.controller;

import com.legallyshop.legallyshop.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    @Value("${spring.app.jwt.secret}")
    private String secret;
    @GetMapping("/api/test/get-all")
    public ResponseEntity<ApiResponse<?>> test() {
        System.out.println(secret);
        return ResponseEntity.ok(ApiResponse.ok("test1"));
    }

    @GetMapping("/api/test/get-al")
    public ApiResponse<String> test2() {
        return ApiResponse.ok("test2");
    }
}
