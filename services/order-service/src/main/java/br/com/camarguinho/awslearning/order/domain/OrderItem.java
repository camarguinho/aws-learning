package br.com.camarguinho.awslearning.order.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Item de um pedido, persistido na tabela {@code order_items} do Amazon RDS
 * (PostgreSQL). Mantém uma cópia do preço no momento da compra (price
 * snapshot) — prática comum para não depender do catálogo para reconstruir o
 * valor histórico de um pedido já fechado.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceSnapshot;

    protected OrderItem() {
    }

    public OrderItem(String productId, Integer quantity, BigDecimal unitPriceSnapshot) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public BigDecimal getSubtotal() {
        return unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }
}
