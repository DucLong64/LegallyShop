package com.legallyshop.legallyshop.order.service;

import com.legallyshop.legallyshop.common.exception.AppException;
import com.legallyshop.legallyshop.notification.service.NotificationService;
import com.legallyshop.legallyshop.order.dto.request.PlaceOrderRequest;
import com.legallyshop.legallyshop.order.dto.response.OrderDetailResponse;
import com.legallyshop.legallyshop.order.dto.response.OrderSummaryResponse;
import com.legallyshop.legallyshop.order.entity.Order;
import com.legallyshop.legallyshop.order.entity.OrderItem;
import com.legallyshop.legallyshop.order.entity.OrderStatus;
import com.legallyshop.legallyshop.order.repository.OrderRepository;
import com.legallyshop.legallyshop.product.entity.Sku;
import com.legallyshop.legallyshop.product.entity.SkuOption;
import com.legallyshop.legallyshop.product.service.SkuService;
import com.legallyshop.legallyshop.user.entity.User;
import com.legallyshop.legallyshop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final SkuService skuService;
    private final NotificationService notificationService;

    // ─── Queries ─────────────────────────────────────────────────────────────

    public Page<OrderSummaryResponse> getMyOrders(Long userId, Pageable pageable) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toSummary);
    }

    public OrderDetailResponse getOrder(Long userId, String orderCode) {
        Order order = orderRepo.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng '" + orderCode + "'"));
        return toDetail(order);
    }

    // ─── Commands ────────────────────────────────────────────────────────────

    @Transactional
    public OrderDetailResponse placeOrder(Long userId, PlaceOrderRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> AppException.notFound("Người dùng"));

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setUser(user);
        order.setReceiverName(req.getReceiverName());
        order.setReceiverPhone(req.getReceiverPhone());
        order.setShippingAddress(req.getShippingAddress());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setNote(req.getNote());

        BigDecimal total = BigDecimal.ZERO;

        for (PlaceOrderRequest.OrderItemRequest itemReq : req.getItems()) {
            Sku sku = skuService.getAvailable(itemReq.getSkuId());

            // Trừ tồn kho
            skuService.decreaseStock(sku.getId(), itemReq.getQuantity());

            // Build snapshot options: "Đen titan / 256GB"
            String skuOptions = sku.getOptions().stream()
                    .map(SkuOption::getOptionValue)
                    .collect(Collectors.joining(" / "));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setSku(sku);
            item.setProductName(sku.getProduct().getName());
            item.setSkuOptions(skuOptions);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(sku.getPrice());
            item.setSubtotal(sku.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));

            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        Order saved = orderRepo.save(order);

        // Gửi email xác nhận (async, không ảnh hưởng main flow)
        notificationService.sendOrderConfirmation(saved);

        return toDetail(saved);
    }

    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));

        if (!order.getUser().getId().equals(userId)) {
            throw AppException.forbidden();
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw AppException.badRequest(
                    "Chỉ có thể hủy đơn hàng ở trạng thái chờ xác nhận");
        }

        // Hoàn lại tồn kho
        for (OrderItem item : order.getItems()) {
            skuService.increaseStock(item.getSku().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    @Transactional
    public OrderDetailResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng"));
        order.setStatus(newStatus);
        return toDetail(orderRepo.save(order));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String generateOrderCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%05d", new Random().nextInt(99999));
        return "ORD-" + date + "-" + suffix;
    }

    private OrderSummaryResponse toSummary(Order o) {
        return OrderSummaryResponse.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .status(o.getStatus().name())
                .paymentMethod(o.getPaymentMethod().name())
                .paymentStatus(o.getPaymentStatus().name())
                .totalAmount(o.getTotalAmount())
                .itemCount(o.getItems().size())
                .createdAt(o.getCreatedAt())
                .build();
    }

    private OrderDetailResponse toDetail(Order o) {
        return OrderDetailResponse.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .status(o.getStatus().name())
                .paymentMethod(o.getPaymentMethod().name())
                .paymentStatus(o.getPaymentStatus().name())
                .totalAmount(o.getTotalAmount())
                .shippingFee(o.getShippingFee())
                .discountAmount(o.getDiscountAmount())
                .receiverName(o.getReceiverName())
                .receiverPhone(o.getReceiverPhone())
                .shippingAddress(o.getShippingAddress())
                .note(o.getNote())
                .items(o.getItems().stream().map(i ->
                        OrderDetailResponse.ItemResponse.builder()
                                .id(i.getId())
                                .skuId(i.getSku().getId())
                                .productName(i.getProductName())
                                .skuOptions(i.getSkuOptions())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .subtotal(i.getSubtotal())
                                .build()).toList())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
