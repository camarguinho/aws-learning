package br.com.camarguinho.awslearning.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do notification-service.
 *
 * <p>Serviços AWS explorados neste microsserviço:</p>
 * <ul>
 *     <li><b>Amazon SQS</b> — consome, via {@code @SqsListener}, os eventos de
 *     pedido entregues pelo fan-out do SNS;</li>
 *     <li><b>Amazon SES</b> — envia o e-mail de confirmação do pedido;</li>
 *     <li><b>Amazon CloudWatch</b> — recebe métricas customizadas
 *     (notificações enviadas/falhas) publicadas pelo Micrometer.</li>
 * </ul>
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
