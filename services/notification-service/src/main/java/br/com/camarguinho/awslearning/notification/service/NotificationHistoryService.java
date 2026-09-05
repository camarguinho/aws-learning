package br.com.camarguinho.awslearning.notification.service;

import br.com.camarguinho.awslearning.notification.domain.NotificationRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Histórico em memória das últimas notificações processadas. Existe apenas
 * para você inspecionar, via {@code GET /api/v1/notifications}, o que o
 * listener do SQS consumiu — em um cenário real esse histórico estaria em um
 * banco de dados ou seria consultado via CloudWatch Logs Insights.
 */
@Service
public class NotificationHistoryService {

    private static final int MAX_SIZE = 100;

    private final Deque<NotificationRecord> history = new ArrayDeque<>(MAX_SIZE);

    public synchronized void record(NotificationRecord record) {
        if (history.size() == MAX_SIZE) {
            history.removeLast();
        }
        history.addFirst(record);
    }

    public synchronized List<NotificationRecord> recent() {
        return Collections.unmodifiableList(List.copyOf(history));
    }
}
