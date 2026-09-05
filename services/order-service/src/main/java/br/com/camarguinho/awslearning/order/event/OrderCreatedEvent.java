package br.com.camarguinho.awslearning.order.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Evento publicado no tópico Amazon SNS {@code order-notifications-topic}
 * quando um pedido é criado com sucesso. O SNS faz fan-out desse evento para
 * todos os assinantes inscritos (no case, uma fila SQS consumida pelo
 * notification-service — nada impede adicionar, por exemplo, uma fila de
 * analytics ou uma Lambda de fraude sem tocar no order-service).
 */
public record OrderCreatedEvent(Long orderId, String customerId, BigDecimal total, Instant occurredAt) {
}
