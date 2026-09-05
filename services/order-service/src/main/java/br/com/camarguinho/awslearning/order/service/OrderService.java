package br.com.camarguinho.awslearning.order.service;

import br.com.camarguinho.awslearning.common.exception.BusinessException;
import br.com.camarguinho.awslearning.common.exception.ResourceNotFoundException;
import br.com.camarguinho.awslearning.order.client.CatalogClient;
import br.com.camarguinho.awslearning.order.client.dto.ProductDto;
import br.com.camarguinho.awslearning.order.domain.Order;
import br.com.camarguinho.awslearning.order.domain.OrderItem;
import br.com.camarguinho.awslearning.order.domain.OrderStatus;
import br.com.camarguinho.awslearning.order.event.OrderCreatedEvent;
import br.com.camarguinho.awslearning.order.event.OrderEventPublisher;
import br.com.camarguinho.awslearning.order.repository.OrderRepository;
import br.com.camarguinho.awslearning.order.web.dto.OrderRequest;
import br.com.camarguinho.awslearning.order.web.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Orquestra a criação de um pedido: valida estoque/preço no catalog-service,
 * persiste o agregado no Amazon RDS e publica o evento de domínio no Amazon
 * SNS. A validação de estoque acontece <em>antes</em> da transação de escrita
 * para não segurar uma conexão de banco enquanto se espera uma chamada de
 * rede.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, CatalogClient catalogClient,
                         OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = new Order(request.customerId());

        request.items().forEach(itemRequest -> {
            ProductDto product = catalogClient.findProduct(itemRequest.productId());
            if (product.stockQuantity() < itemRequest.quantity()) {
                throw new BusinessException("Estoque insuficiente para o produto " + product.productId());
            }
            order.addItem(new OrderItem(product.productId(), itemRequest.quantity(), product.price()));
        });

        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);

        orderEventPublisher.publish(new OrderCreatedEvent(saved.getId(), saved.getCustomerId(),
                saved.getTotal(), Instant.now()));

        return toResponse(saved);
    }

    public OrderResponse findById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + orderId));
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getProductId(), item.getQuantity(), item.getUnitPriceSnapshot(), item.getSubtotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getStatus(),
                order.getTotal(), order.getCreatedAt(), items);
    }
}
