package br.com.camarguinho.awslearning.notification.service;

import br.com.camarguinho.awslearning.notification.event.OrderCreatedEvent;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

/**
 * Envia o e-mail de confirmação de pedido via Amazon SES.
 *
 * <p>O Spring Cloud AWS registra automaticamente um {@link MailSender}
 * (implementado sobre o cliente SES) quando o starter
 * {@code spring-cloud-aws-starter-ses} está no classpath — o código de
 * aplicação usa apenas a interface padrão do Spring, sem conhecer detalhes
 * do SDK da AWS.</p>
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final MailSender mailSender;
    private final String fromAddress;

    public EmailNotificationService(MailSender mailSender,
                                     @Value("${app.ses.from-address}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * Envia a confirmação por e-mail. Tem retry porque o SES pode devolver
     * throttling (limite de envio por segundo) em contas novas/sandbox — uma
     * nova tentativa com pequeno atraso normalmente é suficiente.
     */
    @Retry(name = "ses")
    public void sendOrderConfirmation(OrderCreatedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo("customer+" + event.customerId() + "@example.com");
        message.setSubject("Pedido #" + event.orderId() + " confirmado");
        message.setText("Seu pedido #%d no valor de R$ %s foi confirmado. Obrigado pela compra!"
                .formatted(event.orderId(), event.total()));

        mailSender.send(message);
        log.info("E-mail de confirmação enviado via SES para o pedido {}", event.orderId());
    }
}
