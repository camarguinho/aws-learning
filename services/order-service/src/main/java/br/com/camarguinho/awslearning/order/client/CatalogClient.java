package br.com.camarguinho.awslearning.order.client;

import br.com.camarguinho.awslearning.order.client.dto.ProductDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Chamada síncrona ao catalog-service para validar preço/estoque na hora de
 * fechar um pedido. Combina três padrões de resiliência do Resilience4j:
 *
 * <ul>
 *     <li><b>Retry</b> — repete automaticamente em falhas transitórias de rede;</li>
 *     <li><b>Circuit Breaker</b> — abre o circuito e falha rápido se o
 *     catalog-service estiver consistentemente indisponível, evitando
 *     sobrecarregar um serviço já degradado;</li>
 *     <li><b>Bulkhead</b> — limita quantas chamadas concorrentes a este
 *     endpoint podem ocorrer, isolando o impacto de uma lentidão do
 *     catalog-service do restante do order-service.</li>
 * </ul>
 *
 * Detalhes de configuração (thresholds, tempo de espera) ficam em
 * {@code application.yml}, sob {@code resilience4j.*.instances.catalog-service}.
 */
@Component
public class CatalogClient {

    private final RestClient catalogRestClient;

    public CatalogClient(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    @CircuitBreaker(name = "catalog-service", fallbackMethod = "fallbackFindProduct")
    @Bulkhead(name = "catalog-service")
    @Retry(name = "catalog-service")
    public ProductDto findProduct(String productId) {
        return catalogRestClient.get()
                .uri("/api/v1/products/{id}", productId)
                .retrieve()
                .body(ProductDto.class);
    }

    @SuppressWarnings("unused")
    private ProductDto fallbackFindProduct(String productId, Throwable throwable) {
        throw new CatalogUnavailableException(
                "catalog-service indisponível para o produto " + productId, throwable);
    }
}
