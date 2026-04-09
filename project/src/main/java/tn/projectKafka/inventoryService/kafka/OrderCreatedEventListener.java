package tn.projectKafka.inventoryService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tn.projectKafka.inventoryService.entity.Event;
import tn.projectKafka.inventoryService.event.InventoryUpdatedEvent;
import tn.projectKafka.inventoryService.event.OrderCreatedEvent;
import tn.projectKafka.inventoryService.repository.EventRepository;
import tn.projectKafka.inventoryService.service.InventoryService;

@Service
@Slf4j
public class OrderCreatedEventListener {

    private final InventoryService inventoryService;
    private final EventRepository eventRepository;
    private final KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderCreatedEventListener(InventoryService inventoryService,
                                    EventRepository eventRepository,
                                    KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.eventRepository = eventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order_created_event", groupId = "inventory-service")
    public void onOrderCreated(String message) throws Exception {
        OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(message, OrderCreatedEvent.class);
        log.info("Order created event received: {}", orderCreatedEvent);

        inventoryService.updateEventCapacity(orderCreatedEvent.getEventId(), orderCreatedEvent.getTicketCount());

        Long remainingCapacity = null;
        Event event = eventRepository.findById(orderCreatedEvent.getEventId()).orElse(null);
        if (event != null) {
            remainingCapacity = event.getLeftCapacity();
        }

        InventoryUpdatedEvent inventoryUpdatedEvent = InventoryUpdatedEvent.builder()
                .eventId(orderCreatedEvent.getEventId())
                .ticketsBooked(orderCreatedEvent.getTicketCount())
                .remainingCapacity(remainingCapacity)
                .build();

        kafkaTemplate.send("inventory_updated_event", inventoryUpdatedEvent);
        log.info("Inventory updated event sent to kafka: {}", inventoryUpdatedEvent);
    }
}
