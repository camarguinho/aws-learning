package br.com.camarguinho.awslearning.order.service;

import br.com.camarguinho.awslearning.common.exception.BusinessException;
import br.com.camarguinho.awslearning.order.client.CatalogClient;
import br.com.camarguinho.awslearning.order.client.dto.ProductDto;
import br.com.camarguinho.awslearning.order.domain.Order;
import br.com.camarguinho.awslearning.order.event.OrderEventPublisher;
import br.com.camarguinho.awslearning.order.repository.OrderRepository;
import br.com.camarguinho.awslearning.order.web.dto.OrderItemRequest;
import br.com.camarguinho.awslearning.order.web.dto.OrderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createShouldPublishEventWhenStockIsAvailable() {
        var request = new OrderRequest("customer-1", List.of(new OrderItemRequest("product-1", 2)));
        when(catalogClient.findProduct("product-1"))
                .thenReturn(new ProductDto("product-1", "Mouse", new BigDecimal("100.00"), 10));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.create(request);

        assertThat(response.total()).isEqualByComparingTo("200.00");
        verify(orderEventPublisher).publish(any());
    }

    @Test
    void createShouldFailWhenStockIsInsufficient() {
        var request = new OrderRequest("customer-1", List.of(new OrderItemRequest("product-1", 99)));
        when(catalogClient.findProduct(anyString()))
                .thenReturn(new ProductDto("product-1", "Mouse", new BigDecimal("100.00"), 1));

        assertThatThrownBy(() -> orderService.create(request)).isInstanceOf(BusinessException.class);
    }
}
