package br.com.camarguinho.awslearning.order.repository;

import br.com.camarguinho.awslearning.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso JPA padrão à tabela {@code orders} no Amazon RDS. */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
