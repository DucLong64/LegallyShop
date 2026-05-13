package com.legallyshop.legallyshop.cart.controller;

import com.legallyshop.legallyshop.cart.dto.AddCartItemRequest;
import com.legallyshop.legallyshop.cart.dto.CartResponse;
import com.legallyshop.legallyshop.cart.service.CartService;
import com.legallyshop.legallyshop.common.response.ApiResponse;
import com.legallyshop.legallyshop.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Giỏ hàng")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Lấy giỏ hàng hiện tại")
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.ok(cartService.getCart(user.getId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ")
    public ApiResponse<CartResponse> addItem(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody AddCartItemRequest req) {
        cartService.addItem(user.getId(), req.getSkuId(), req.getQty());
        return ApiResponse.ok(cartService.getCart(user.getId()));
    }

    @PutMapping("/items/{skuId}")
    @Operation(summary = "Cập nhật số lượng item trong giỏ")
    public ApiResponse<CartResponse> updateItem(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long skuId,
            @RequestParam int qty) {
        cartService.updateItem(user.getId(), skuId, qty);
        return ApiResponse.ok(cartService.getCart(user.getId()));
    }

    @DeleteMapping("/items/{skuId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ")
    public ApiResponse<CartResponse> removeItem(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long skuId) {
        cartService.removeItem(user.getId(), skuId);
        return ApiResponse.ok(cartService.getCart(user.getId()));
    }

    @DeleteMapping
    @Operation(summary = "Xóa toàn bộ giỏ hàng")
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal UserPrincipal user) {
        cartService.clearCart(user.getId());
        return ApiResponse.ok(null);
    }
}
