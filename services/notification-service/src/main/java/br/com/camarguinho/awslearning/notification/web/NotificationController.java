package br.com.camarguinho.awslearning.notification.web;

import br.com.camarguinho.awslearning.notification.domain.NotificationRecord;
import br.com.camarguinho.awslearning.notification.service.NotificationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint apenas de leitura para inspecionar as últimas notificações
 * processadas a partir do consumo do SQS — útil para validar, durante o
 * estudo, que o fluxo SNS -&gt; SQS -&gt; SES está funcionando.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Histórico de notificações consumidas do SQS/SNS e enviadas via SES")
public class NotificationController {

    private final NotificationHistoryService notificationHistoryService;

    public NotificationController(NotificationHistoryService notificationHistoryService) {
        this.notificationHistoryService = notificationHistoryService;
    }

    @GetMapping
    @Operation(summary = "Lista as últimas notificações processadas (em memória)")
    public List<NotificationRecord> recent() {
        return notificationHistoryService.recent();
    }
}
