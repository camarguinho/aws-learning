package br.com.camarguinho.awslearning.notification.listener;

import br.com.camarguinho.awslearning.notification.domain.NotificationRecord;
import br.com.camarguinho.awslearning.notification.event.OrderCreatedEvent;
import br.com.camarguinho.awslearning.notification.service.EmailNotificationService;
import br.com.camarguinho.awslearning.notification.service.NotificationHistoryService;
import br.com.camarguinho.awslearning.notification.service.NotificationMetrics;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumidor da fila Amazon SQS {@code order-events-queue}.
 *
 * <p>A fila está inscrita (com raw message delivery) no tópico SNS
 * {@code order-notifications-topic}: o order-service não conhece este
 * serviço, apenas publica no tópico — é o SNS/SQS que desacoplam produtor e
 * consumidor. Se a mensagem falhar repetidamente, o Spring Cloud AWS SQS a
 * devolve à fila até esgotar {@code maxReceiveCount}, quando então a
 * própria fila a move automaticamente para a Dead Letter Queue
 * ({@code order-events-dlq}) configurada na infraestrutura.</p>
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final EmailNotificationService emailNotificationService;
    private final NotificationHistoryService notificationHistoryService;
    private final NotificationMetrics notificationMetrics;

    public OrderEventListener(EmailNotificationService emailNotificationService,
                               NotificationHistoryService notificationHistoryService,
                               NotificationMetrics notificationMetrics) {
        this.emailNotificationService = emailNotificationService;
        this.notificationHistoryService = notificationHistoryService;
        this.notificationMetrics = notificationMetrics;
    }

    @SqsListener("order-events-queue")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Evento OrderCreatedEvent recebido do SQS para o pedido {}", event.orderId());
        try {
            emailNotificationService.sendOrderConfirmation(event);
            notificationMetrics.incrementProcessed();
            notificationHistoryService.record(new NotificationRecord(
                    event.orderId(), event.customerId(), "EMAIL", "SENT", Instant.now()));
        } catch (Exception ex) {
            notificationMetrics.incrementFailed();
            notificationHistoryService.record(new NotificationRecord(
                    event.orderId(), event.customerId(), "EMAIL", "FAILED", Instant.now()));
            // Relança para que o SQS não confirme (ack) a mensagem: ela volta
            // à fila e, após maxReceiveCount tentativas, cai na DLQ.
            throw ex;
        }
    }
}
