package tn.projectkafka.bookingService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tn.projectkafka.bookingService.event.InventoryUpdatedEvent;

@Service
@Slf4j
public class InventoryUpdatedEventListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "inventory_updated_event", groupId = "booking-service")
    public void onInventoryUpdated(String message) throws Exception {
        InventoryUpdatedEvent event = objectMapper.readValue(message, InventoryUpdatedEvent.class);
        log.info("Inventory updated event received: {}", event);
    }
}
