package tn.projectkafka.bookingService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.projectkafka.bookingService.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
