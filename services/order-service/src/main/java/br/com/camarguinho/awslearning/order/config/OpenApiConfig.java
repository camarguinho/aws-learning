package br.com.camarguinho.awslearning.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados exibidos no Swagger UI (/swagger-ui.html) deste serviço. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Order Service API")
                .version("v1")
                .description("Pedidos do case de e-commerce AWS. RDS (PostgreSQL), " +
                        "Secrets Manager, Parameter Store e SNS."));
    }
}
