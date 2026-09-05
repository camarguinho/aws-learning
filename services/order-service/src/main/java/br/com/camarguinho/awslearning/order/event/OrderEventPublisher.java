package br.com.camarguinho.awslearning.order.event;

import io.awspring.cloud.sns.core.SnsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de domínio no Amazon SNS usando o {@link SnsTemplate} do
 * Spring Cloud AWS — a mesma ideia de {@code JmsTemplate}/{@code KafkaTemplate},
 * mas para o serviço de pub/sub gerenciado da AWS.
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final SnsTemplate snsTemplate;
    private final String topicName;

    public OrderEventPublisher(SnsTemplate snsTemplate,
                                @Value("${app.sns.order-notifications-topic}") String topicName) {
        this.snsTemplate = snsTemplate;
        this.topicName = topicName;
    }

    public void publish(OrderCreatedEvent event) {
        snsTemplate.convertAndSend(topicName, event);
        log.info("Evento OrderCreatedEvent publicado no SNS para o pedido {}", event.orderId());
    }
}
