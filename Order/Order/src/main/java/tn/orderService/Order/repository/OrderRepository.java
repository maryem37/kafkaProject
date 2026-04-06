package tn.orderService.Order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.orderService.Order.entity.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
