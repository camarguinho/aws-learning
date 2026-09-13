package br.com.camarguinho.awslearning.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados exibidos no Swagger UI (/swagger-ui.html) deste serviço. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Catalog Service API")
                .version("v1")
                .description("Catálogo de produtos do case de e-commerge AWS. " +
                        "Persistência em DynamoDB, imagens em S3, cache em ElastiCache (Redis)."));
    }
}
