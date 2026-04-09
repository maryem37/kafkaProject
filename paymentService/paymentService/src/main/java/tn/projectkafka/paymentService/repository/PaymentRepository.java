package tn.projectkafka.paymentService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.projectkafka.paymentService.entity.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByOrderId(Long orderId);
}
