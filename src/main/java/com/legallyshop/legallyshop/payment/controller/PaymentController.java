package com.legallyshop.legallyshop.payment.controller;

import com.legallyshop.legallyshop.common.response.ApiResponse;
import com.legallyshop.legallyshop.payment.service.VnPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Thanh toán")
public class PaymentController {

    private final VnPayService vnPayService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @GetMapping("/vnpay/create")
    @Operation(summary = "Tạo URL thanh toán VNPay")
    public ApiResponse<String> createVnPayUrl(
            @RequestParam String orderCode,
            HttpServletRequest request) {
        return ApiResponse.ok(vnPayService.createPaymentUrl(orderCode, request));
    }

    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay callback sau khi thanh toán")
    public void vnpayCallback(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {
        boolean success = vnPayService.handleCallback(params);
        String redirectUrl = success
                ? frontendUrl + "/order/success?order=" + params.get("vnp_TxnRef")
                : frontendUrl + "/order/failed?order=" + params.get("vnp_TxnRef");
        response.sendRedirect(redirectUrl);
    }
}
