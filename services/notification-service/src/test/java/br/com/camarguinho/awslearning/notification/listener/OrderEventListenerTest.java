package br.com.camarguinho.awslearning.notification.listener;

import br.com.camarguinho.awslearning.notification.event.OrderCreatedEvent;
import br.com.camarguinho.awslearning.notification.service.EmailNotificationService;
import br.com.camarguinho.awslearning.notification.service.NotificationHistoryService;
import br.com.camarguinho.awslearning.notification.service.NotificationMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    @Mock
    private NotificationMetrics notificationMetrics;

    @InjectMocks
    private OrderEventListener orderEventListener;

    @Test
    void shouldIncrementFailedMetricAndRethrowWhenEmailSendingFails() {
        var event = new OrderCreatedEvent(1L, "customer-1", new BigDecimal("50.00"), Instant.now());
        doThrow(new RuntimeException("SES indisponível")).when(emailNotificationService).sendOrderConfirmation(event);

        assertThatThrownBy(() -> orderEventListener.onOrderCreated(event)).isInstanceOf(RuntimeException.class);

        verify(notificationMetrics).incrementFailed();
        verify(notificationHistoryService).record(org.mockito.ArgumentMatchers.any());
    }
}
