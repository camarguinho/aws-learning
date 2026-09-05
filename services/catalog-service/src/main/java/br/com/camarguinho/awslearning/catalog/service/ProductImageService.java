package br.com.camarguinho.awslearning.catalog.service;

import io.awspring.cloud.s3.S3Template;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;

/**
 * Gera URLs pré-assinadas (presigned URLs) para o bucket S3 de imagens de
 * produto.
 *
 * <p>Padrão cloud native aplicado aqui: o upload/download binário do arquivo
 * <b>não passa pelo microsserviço</b>. O serviço apenas assina uma URL
 * temporária e o cliente (browser/app) fala diretamente com o S3. Isso evita
 * consumir CPU/memória/banda do microsserviço com transferência de arquivos e
 * permite escalar a camada de negócio independentemente do volume de
 * imagens.</p>
 */
@Service
public class ProductImageService {

    private final S3Template s3Template;
    private final String bucketName;

    public ProductImageService(S3Template s3Template,
                                @Value("${app.s3.product-images-bucket}") String bucketName) {
        this.s3Template = s3Template;
        this.bucketName = bucketName;
    }

    /**
     * Cria uma URL pré-assinada de leitura (GET), válida por 15 minutos, para
     * o objeto informado. Protegida por circuit breaker + retry: se o S3
     * estiver instável, falha rápido em vez de travar a resposta da API de
     * catálogo — a foto do produto é degradável, o preço e o estoque não são.
     */
    @CircuitBreaker(name = "s3", fallbackMethod = "fallbackSignedUrl")
    @Retry(name = "s3")
    public String createReadUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        URL url = s3Template.createSignedGetURL(bucketName, imageKey, Duration.ofMinutes(15));
        return url.toString();
    }

    /**
     * Cria uma URL pré-assinada de escrita (PUT), válida por 5 minutos, para
     * que o cliente faça upload direto da imagem para o S3.
     */
    @CircuitBreaker(name = "s3", fallbackMethod = "fallbackSignedUrl")
    @Retry(name = "s3")
    public String createUploadUrl(String imageKey) {
        URL url = s3Template.createSignedPutURL(bucketName, imageKey, Duration.ofMinutes(5));
        return url.toString();
    }

    @SuppressWarnings("unused")
    private String fallbackSignedUrl(String imageKey, Throwable throwable) {
        return null;
    }
}
