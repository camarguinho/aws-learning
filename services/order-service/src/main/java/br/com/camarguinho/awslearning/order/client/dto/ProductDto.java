package br.com.camarguinho.awslearning.order.client.dto;

import java.math.BigDecimal;

/** Subconjunto da resposta do catalog-service relevante para o order-service. */
public record ProductDto(String productId, String name, BigDecimal price, Integer stockQuantity) {
}
