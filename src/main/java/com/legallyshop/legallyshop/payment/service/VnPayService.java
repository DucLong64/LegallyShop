package com.legallyshop.legallyshop.payment.service;

import com.legallyshop.legallyshop.common.exception.AppException;
import com.legallyshop.legallyshop.order.entity.Order;
import com.legallyshop.legallyshop.order.entity.PaymentStatus;
import com.legallyshop.legallyshop.order.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;
    @Value("${vnpay.hash-secret}")
    private String hashSecret;
    @Value("${vnpay.url}")
    private String vnpayUrl;
    @Value("${vnpay.return-url}")
    private String returnUrl;

    private final OrderRepository orderRepo;

    // ─── Tạo URL thanh toán ───────────────────────────────────────────────────

    public String createPaymentUrl(String orderCode, HttpServletRequest request) {
        Order order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> AppException.notFound("Đơn hàng '" + orderCode + "'"));

        // Số tiền VNPay tính theo đơn vị VNĐ * 100
        long amount = order.getTotalAmount()
                .add(order.getShippingFee())
                .subtract(order.getDiscountAmount())
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderCode);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderCode);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", getClientIp(request));
        params.put("vnp_CreateDate", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String queryString = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        String secureHash = hmacSha512(hashSecret, queryString);
        return vnpayUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    // ─── Xử lý callback từ VNPay ─────────────────────────────────────────────

    @Transactional
    public boolean handleCallback(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        String orderCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        // Verify chữ ký
        Map<String, String> filtered = new TreeMap<>(params);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");

        String queryString = filtered.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        String expectedHash = hmacSha512(hashSecret, queryString);

        if (!expectedHash.equalsIgnoreCase(receivedHash)) {
            log.warn("VNPay callback: invalid signature for order {}", orderCode);
            return false;
        }

        Order order = orderRepo.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            log.warn("VNPay callback: order not found {}", orderCode);
            return false;
        }

        if ("00".equals(responseCode)) {
            order.setPaymentStatus(PaymentStatus.PAID);
            log.info("VNPay payment success for order {}", orderCode);
            return true;
        } else {
            log.info("VNPay payment failed for order {}, code={}", orderCode, responseCode);
            return false;
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA512 error", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }
}
