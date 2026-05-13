package com.legallyshop.legallyshop.notification.service;

import com.legallyshop.legallyshop.order.entity.Order;
import com.legallyshop.legallyshop.order.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    private static final NumberFormat VND = NumberFormat.getNumberInstance(
            new Locale("vi", "VN"));

    public void sendOrderConfirmation(Order order) {
        if (order.getUser() == null || order.getUser().getEmail() == null) return;

        String subject = "✅ Xác nhận đơn hàng #" + order.getOrderCode();
        String body = buildOrderConfirmationEmail(order);
        emailService.send(order.getUser().getEmail(), subject, body);
    }

    public void sendOrderStatusUpdate(Order order) {
        if (order.getUser() == null || order.getUser().getEmail() == null) return;

        String statusLabel = switch (order.getStatus()) {
            case CONFIRMED -> "đã được xác nhận";
            case SHIPPING -> "đang được giao đến bạn";
            case DELIVERED -> "đã giao thành công";
            case CANCELLED -> "đã bị hủy";
            default -> "đã cập nhật";
        };

        String subject = "📦 Đơn hàng #" + order.getOrderCode() + " " + statusLabel;
        String body = "<h2>Cập nhật đơn hàng</h2>"
                + "<p>Đơn hàng <strong>#" + order.getOrderCode() + "</strong> "
                + statusLabel + ".</p>";
        emailService.send(order.getUser().getEmail(), subject, body);
    }

    // ─── Template builder ─────────────────────────────────────────────────────

    private String buildOrderConfirmationEmail(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:600px;margin:auto'>");
        sb.append("<h2 style='color:#2563eb'>Cảm ơn bạn đã đặt hàng!</h2>");
        sb.append("<p>Mã đơn hàng: <strong>").append(order.getOrderCode()).append("</strong></p>");
        sb.append("<p>Người nhận: <strong>").append(order.getReceiverName()).append("</strong></p>");
        sb.append("<p>Địa chỉ: ").append(order.getShippingAddress()).append("</p>");
        sb.append("<hr/>");
        sb.append("<h3>Sản phẩm đã đặt</h3>");
        sb.append("<table width='100%' cellpadding='8' style='border-collapse:collapse'>");
        sb.append("<tr style='background:#f3f4f6'>");
        sb.append("<th align='left'>Sản phẩm</th>");
        sb.append("<th align='center'>SL</th>");
        sb.append("<th align='right'>Thành tiền</th>");
        sb.append("</tr>");

        for (OrderItem item : order.getItems()) {
            sb.append("<tr style='border-bottom:1px solid #e5e7eb'>");
            sb.append("<td>").append(item.getProductName());
            if (item.getSkuOptions() != null && !item.getSkuOptions().isBlank()) {
                sb.append("<br/><small style='color:#6b7280'>").append(item.getSkuOptions()).append("</small>");
            }
            sb.append("</td>");
            sb.append("<td align='center'>").append(item.getQuantity()).append("</td>");
            sb.append("<td align='right'>").append(VND.format(item.getSubtotal())).append("₫</td>");
            sb.append("</tr>");
        }

        sb.append("</table><hr/>");
        sb.append("<p align='right'><strong>Tổng tiền: ")
                .append(VND.format(order.getTotalAmount())).append("₫</strong></p>");
        sb.append("<p style='color:#6b7280;font-size:13px'>Chúng tôi sẽ liên hệ xác nhận đơn sớm nhất có thể.</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}