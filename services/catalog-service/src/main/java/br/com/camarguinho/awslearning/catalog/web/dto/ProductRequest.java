package br.com.camarguinho.awslearning.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Payload de criação/atualização de um produto.
 */
@Schema(description = "Dados de entrada para criar ou atualizar um produto")
public record ProductRequest(

        @NotBlank
        @Schema(example = "Teclado Mecânico RGB")
        String name,

        @Schema(example = "Teclado mecânico com switches azuis e iluminação RGB")
        String description,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Schema(example = "349.90")
        BigDecimal price,

        @NotNull
        @Min(0)
        @Schema(example = "120")
        Integer stockQuantity
) {
}
