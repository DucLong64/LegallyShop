package com.legallyshop.legallyshop.order.controller;

import com.legallyshop.legallyshop.common.response.ApiResponse;
import com.legallyshop.legallyshop.order.dto.request.PlaceOrderRequest;
import com.legallyshop.legallyshop.order.dto.response.OrderDetailResponse;
import com.legallyshop.legallyshop.order.dto.response.OrderSummaryResponse;
import com.legallyshop.legallyshop.order.entity.OrderStatus;
import com.legallyshop.legallyshop.order.service.OrderService;
import com.legallyshop.legallyshop.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Quản lý đơn hàng")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đặt hàng")
    public ApiResponse<OrderDetailResponse> placeOrder(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody PlaceOrderRequest req) {
        return ApiResponse.created(orderService.placeOrder(user.getId(), req));
    }

    @GetMapping
    @Operation(summary = "Lịch sử đơn hàng của tôi")
    public ApiResponse<Page<OrderSummaryResponse>> myOrders(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(orderService.getMyOrders(user.getId(), pageable));
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Chi tiết đơn hàng")
    public ApiResponse<OrderDetailResponse> getOrder(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String orderCode) {
        return ApiResponse.ok(orderService.getOrder(user.getId(), orderCode));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Hủy đơn hàng (chỉ khi PENDING)")
    public ApiResponse<Void> cancel(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id) {
        orderService.cancel(user.getId(), id);
        return ApiResponse.ok(null);
    }

    // ─── Admin endpoints ──────────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (Admin)")
    public ApiResponse<OrderDetailResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ApiResponse.ok(orderService.updateStatus(id, status));
    }
}
