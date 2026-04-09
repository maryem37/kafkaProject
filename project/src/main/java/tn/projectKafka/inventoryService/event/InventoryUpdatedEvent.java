package tn.projectKafka.inventoryService.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryUpdatedEvent {

    private Long eventId;
    private Long ticketsBooked;
    private Long remainingCapacity;
}
