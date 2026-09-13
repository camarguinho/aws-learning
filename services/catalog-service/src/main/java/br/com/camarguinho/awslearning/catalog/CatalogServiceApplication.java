package br.com.camarguinho.awslearning.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Ponto de entrada do catalog-service.
 *
 * <p>Este microsserviço concentra três serviços AWS explorados no
 * capítulo de arquitetura do ebook:</p>
 * <ul>
 *     <li><b>Amazon DynamoDB</b> — persistência do catálogo de produtos;</li>
 *     <li><b>Amazon S3</b> — armazenamento das imagens dos produtos;</li>
 *     <li><b>Amazon ElastiCache (Redis)</b> — cache de leitura para reduzir
 *     latência e custo de leitura no DynamoDB.</li>
 * </ul>
 */
@SpringBootApplication
@EnableCaching
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
