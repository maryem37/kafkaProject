package tn.projectkafka.bookingService.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdatedEvent {

    private Long eventId;
    private Long ticketsBooked;
    private Long remainingCapacity;
}
