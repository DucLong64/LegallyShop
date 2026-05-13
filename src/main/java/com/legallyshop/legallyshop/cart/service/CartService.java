package com.legallyshop.legallyshop.cart.service;

import com.legallyshop.legallyshop.cart.dto.CartResponse;
import com.legallyshop.legallyshop.common.exception.AppException;
import com.legallyshop.legallyshop.product.entity.ProductImage;
import com.legallyshop.legallyshop.product.entity.Sku;
import com.legallyshop.legallyshop.product.entity.SkuOption;
import com.legallyshop.legallyshop.product.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String KEY_PREFIX = "cart:user:";
    private static final Duration TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Object> redis;
    private final SkuRepository skuRepo;

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    public CartResponse getCart(Long userId) {
        Map<Object, Object> entries = redis.opsForHash().entries(key(userId));

        if (entries.isEmpty()) {
            return CartResponse.builder()
                    .totalItems(0)
                    .totalPrice(BigDecimal.ZERO)
                    .items(List.of())
                    .build();
        }

        // Load SKU info từ DB để lấy tên, giá, ảnh
        List<Long> skuIds = entries.keySet().stream()
                .map(k -> Long.valueOf(k.toString()))
                .toList();

        Map<Long, Sku> skuMap = skuRepo.findAllById(skuIds).stream()
                .collect(Collectors.toMap(Sku::getId, s -> s));

        List<CartResponse.CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalQty = 0;

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long skuId = Long.valueOf(entry.getKey().toString());
            int qty = Integer.parseInt(entry.getValue().toString());
            Sku sku = skuMap.get(skuId);

            if (sku == null || !Boolean.TRUE.equals(sku.getIsActive())) continue;

            String options = sku.getOptions().stream()
                    .map(SkuOption::getOptionValue)
                    .collect(Collectors.joining(" / "));

            String imageUrl = sku.getProduct().getImages().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                    .findFirst()
                    .map(ProductImage::getUrl)
                    .orElse(null);

            BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(qty));

            items.add(CartResponse.CartItemResponse.builder()
                    .skuId(skuId)
                    .productName(sku.getProduct().getName())
                    .skuCode(sku.getSkuCode())
                    .skuOptions(options)
                    .price(sku.getPrice())
                    .quantity(qty)
                    .subtotal(subtotal)
                    .imageUrl(imageUrl)
                    .build());

            total = total.add(subtotal);
            totalQty += qty;
        }

        return CartResponse.builder()
                .totalItems(totalQty)
                .totalPrice(total)
                .items(items)
                .build();
    }

    // ─── Commands ────────────────────────────────────────────────────────────

    public void addItem(Long userId, Long skuId, int qty) {
        // Kiểm tra SKU tồn tại và còn hàng
        Sku sku = skuRepo.findById(skuId)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));

        if (!Boolean.TRUE.equals(sku.getIsActive())) {
            throw AppException.badRequest("Sản phẩm này hiện không còn bán");
        }

        redis.opsForHash().increment(key(userId), skuId.toString(), qty);
        redis.expire(key(userId), TTL);
    }

    public void updateItem(Long userId, Long skuId, int qty) {
        if (qty <= 0) {
            removeItem(userId, skuId);
            return;
        }
        redis.opsForHash().put(key(userId), skuId.toString(), String.valueOf(qty));
        redis.expire(key(userId), TTL);
    }

    public void removeItem(Long userId, Long skuId) {
        redis.opsForHash().delete(key(userId), skuId.toString());
    }

    public void clearCart(Long userId) {
        redis.delete(key(userId));
    }
}

