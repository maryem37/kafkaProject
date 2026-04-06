package tn.projectKafka.inventoryService.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.projectKafka.inventoryService.entity.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue,Long> {

}
