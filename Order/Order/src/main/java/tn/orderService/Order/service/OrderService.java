package tn.orderService.Order.service;

import example.tn.orderService.Order.event.BookingEvent;
import example.tn.orderService.Order.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.orderService.Order.client.InventoryServiceClient;
import tn.orderService.Order.entity.Order;
import tn.orderService.Order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@Slf4j
public class OrderService {

    private OrderRepository orderRepository;
    private InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    public OrderService(OrderRepository orderRepository,
                        InventoryServiceClient inventoryServiceClient,
                        KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "booking_event", groupId = "order-service")
    public void orderEvent(String message) throws Exception {
        BookingEvent bookingEvent = new ObjectMapper().readValue(message, BookingEvent.class);
        log.info("Order Event Received: {}", bookingEvent);

        Order order = createOrder(bookingEvent);
        orderRepository.saveAndFlush(order);

        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getCustomerId())
                .eventId(order.getEventId())
                .ticketCount(order.getTicketCount())
                .totalPrice(order.getTotalPrice())
                .build();

        kafkaTemplate.send("order_created_event", orderCreatedEvent);
        log.info("Order created event sent to kafka: {}", orderCreatedEvent);

        inventoryServiceClient.updateInventory(order.getEventId(), order.getTicketCount());
    }

    private Order createOrder(BookingEvent bookingEvent){
        return Order.builder()
                .customerId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();

    }
}