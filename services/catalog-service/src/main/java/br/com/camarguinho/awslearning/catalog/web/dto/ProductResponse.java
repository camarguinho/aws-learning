package br.com.camarguinho.awslearning.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Representação de um produto devolvida pela API. {@code imageUrl} é uma URL
 * pré-assinada do S3, válida por tempo limitado — nunca é a URL pública do
 * bucket (o bucket de imagens não é público).
 */
@Schema(description = "Produto do catálogo")
public record ProductResponse(
        String productId,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        @Schema(description = "URL pré-assinada do S3, válida por 15 minutos") String imageUrl) {
}
