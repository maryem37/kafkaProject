package tn.orderService.Order.service;

import example.tn.orderService.Order.event.BookingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.orderService.Order.client.InventoryServiceClient;
import tn.orderService.Order.entity.Order;
import tn.orderService.Order.repository.OrderRepository;
import tools.jackson.databind.ObjectMapper;
@Service
@Slf4j
public class OrderService {

    private OrderRepository orderRepository;
    private InventoryServiceClient inventoryServiceClient;
    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    public OrderService(OrderRepository orderRepository,
                        InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "booking_event", groupId = "order-service")
    public void orderEvent(String message) throws Exception {
        BookingEvent bookingEvent = new ObjectMapper().readValue(message, BookingEvent.class);
        log.info("Order Event Received: {}", bookingEvent);

        Order order = createOrder(bookingEvent);
        orderRepository.saveAndFlush(order);  // only once

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