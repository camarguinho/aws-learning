package br.com.camarguinho.awslearning.order.web.dto;

import br.com.camarguinho.awslearning.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String customerId,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt,
        List<OrderItemResponse> items) {

    public record OrderItemResponse(String productId, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }
}
