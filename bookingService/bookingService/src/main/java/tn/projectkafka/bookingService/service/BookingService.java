package tn.projectkafka.bookingService.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tn.projectkafka.bookingService.client.InventoryServiceClient;
import tn.projectkafka.bookingService.entity.Customer;
import tn.projectkafka.bookingService.event.BookingEvent;
import tn.projectkafka.bookingService.repository.CustomerRepository;
import tn.projectkafka.bookingService.request.BookingRequest;
import tn.projectkafka.bookingService.response.BookingResponse;
import tn.projectkafka.bookingService.response.InventoryResponse;

import java.math.BigDecimal;

@Service
@Slf4j
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    @Autowired
    public BookingService(final CustomerRepository customerRepository,
                          final InventoryServiceClient inventoryServiceClient,
                          final KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    public BookingResponse createBooking(BookingRequest request) {
        //check if the user exists

        final Customer customer = customerRepository
                .findById(request.getUserId())
                .orElse(null);
        if (customer == null) {
            throw  new RuntimeException("Customer not found");
        }
        //check if there is enough inventory
        final InventoryResponse inventoryResponse =inventoryServiceClient.getInventory(request.getEventId());
        System.out.println("inventory Service Response"+inventoryResponse);
        log.info("inventory Service Response {}",inventoryResponse);
        if(inventoryResponse.getCapacity()<request.getTicketCount()){
            throw  new RuntimeException("Capacity Exceeded");
        }
        //get event information to also get Venue information
        //create booking
        final BookingEvent bookingEvent =createBookingEvent(request,customer,inventoryResponse);

        //send booking to order service on a kafka Topic
        kafkaTemplate.send("booking_event",bookingEvent);
        log.info("booking event sent to kafka{}",bookingEvent);

        return  BookingResponse.builder()
                .userId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();





        }
    private BookingEvent createBookingEvent(
            final BookingRequest request,
            final Customer customer,
            final InventoryResponse inventoryResponse
    ){
        BigDecimal totalPrice = BigDecimal.valueOf(request.getTicketCount())
                .multiply(inventoryResponse.getTicketPrice());

        return BookingEvent.builder()
                .userId(customer.getId())
                .eventId(request.getEventId())
                .ticketCount(request.getTicketCount())
                .totalPrice(totalPrice)
                .build();
    }
}
