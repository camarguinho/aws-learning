package br.com.camarguinho.awslearning.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Registra listeners de eventos do Resilience4j para tornar visível, via log,
 * cada transição de estado de circuit breaker (CLOSED -&gt; OPEN -&gt;
 * HALF_OPEN) e cada nova tentativa de retry. Em produção esses eventos
 * seriam publicados como métricas no CloudWatch (ver {@code notification-service}
 * e o capítulo de Observabilidade do ebook); aqui eles ficam no log para fins
 * didáticos, para você observar o padrão de resiliência funcionando na prática.
 */
@Configuration
public class Resilience4jEventLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(Resilience4jEventLoggingConfig.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public Resilience4jEventLoggingConfig(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void registerListeners() {
        circuitBreakerRegistry.getEventPublisher().onEntryAdded(entryAddedEvent -> {
            var circuitBreaker = entryAddedEvent.getAddedEntry();
            circuitBreaker.getEventPublisher().onStateTransition(event ->
                    log.warn("[CircuitBreaker:{}] transição de estado: {} -> {}",
                            circuitBreaker.getName(),
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState()));
        });

        retryRegistry.getEventPublisher().onEntryAdded(entryAddedEvent -> {
            var retry = entryAddedEvent.getAddedEntry();
            retry.getEventPublisher().onRetry(event ->
                    log.info("[Retry:{}] tentativa #{} após falha: {}",
                            retry.getName(), event.getNumberOfRetryAttempts(),
                            event.getLastThrowable() != null ? event.getLastThrowable().getMessage() : "n/a"));
        });
    }
}
