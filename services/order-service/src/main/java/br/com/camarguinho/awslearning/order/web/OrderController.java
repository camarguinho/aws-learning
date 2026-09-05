package br.com.camarguinho.awslearning.order.web;

import br.com.camarguinho.awslearning.order.service.OrderService;
import br.com.camarguinho.awslearning.order.web.dto.OrderRequest;
import br.com.camarguinho.awslearning.order.web.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API REST de pedidos. Documentada no Swagger UI em {@code /swagger-ui.html}. */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Pedidos (Amazon RDS + Secrets Manager + Parameter Store + SNS)")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um pedido: valida estoque no catalog-service, persiste no RDS e publica no SNS")
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Busca um pedido por id")
    public OrderResponse findById(@PathVariable Long orderId) {
        return orderService.findById(orderId);
    }

    @GetMapping
    @Operation(summary = "Lista todos os pedidos")
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }
}
