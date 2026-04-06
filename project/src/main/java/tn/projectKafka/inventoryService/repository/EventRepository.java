package tn.projectKafka.inventoryService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.projectKafka.inventoryService.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event,Long> {
}
