package br.com.camarguinho.awslearning.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP para o catalog-service. Em uma malha de serviços real, esse
 * endereço viria de service discovery (ex.: Cloud Map ou o DNS interno do
 * ECS/EKS); aqui usamos uma property simples para manter o foco nos serviços
 * AWS de dados/mensageria.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient catalogRestClient(@Value("${app.catalog-service.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
