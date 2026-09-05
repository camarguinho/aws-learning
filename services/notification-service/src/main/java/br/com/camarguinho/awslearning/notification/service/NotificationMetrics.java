package br.com.camarguinho.awslearning.notification.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Métricas de negócio publicadas no Amazon CloudWatch através do
 * {@code CloudWatchMeterRegistry} do Micrometer (autoconfigurado pela
 * dependência {@code micrometer-registry-cloudwatch2} + as propriedades
 * {@code management.cloudwatch.metrics.export.*}). Nenhuma chamada direta ao
 * SDK do CloudWatch é necessária no código de negócio.
 */
@Component
public class NotificationMetrics {

    private final MeterRegistry meterRegistry;

    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementProcessed() {
        meterRegistry.counter("notifications.processed").increment();
    }

    public void incrementFailed() {
        meterRegistry.counter("notifications.failed").increment();
    }
}
