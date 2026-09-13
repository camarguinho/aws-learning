package br.com.camarguinho.awslearning.catalog.web.dto;

/**
 * URL pré-assinada de upload (PUT) para o cliente enviar a imagem do produto
 * diretamente ao Amazon S3, sem transitar pelo microsserviço.
 */
public record ImageUploadUrlResponse(String uploadUrl) {
}
