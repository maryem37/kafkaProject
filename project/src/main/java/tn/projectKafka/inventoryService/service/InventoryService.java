package tn.projectKafka.inventoryService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import tn.projectKafka.inventoryService.entity.Event;
import tn.projectKafka.inventoryService.entity.Venue;
import tn.projectKafka.inventoryService.repository.EventRepository;
import tn.projectKafka.inventoryService.repository.VenueRepository;
import tn.projectKafka.inventoryService.response.EventInventoryResponse;
import tn.projectKafka.inventoryService.response.VenueInventoryResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    @Autowired
    public InventoryService(final EventRepository eventRepository,
                            final VenueRepository venueRepository){
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    public List<EventInventoryResponse> getAllEvents(){

        final List<Event> events = eventRepository.findAll();

        return events.stream()
                .map(event -> EventInventoryResponse.builder()
                .eventId(event.getId())
                        .event(event.getName())
                .capacity(event.getLeftCapacity())
                        .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                        .build()
                ).collect(Collectors.toList());
    }

    public VenueInventoryResponse getVenueInformation(final Long venueId){

        final Venue venue = venueRepository.findById(venueId).orElse(null);

        return VenueInventoryResponse.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalCapacity(venue.getTotalCapacity())
                .build();
    }

    public EventInventoryResponse getEventInventory(final Long eventId){
        final Event event = eventRepository.findById(eventId).orElse(null);
        return EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getLeftCapacity())
                .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                .eventId(event.getId())
                .build();
    }


//    @PutMapping("/event/{eventId}/capacity/{ticketsBooked}")
//    public ResponseEntity<Void> updateCapacity(
//            @PathVariable Long eventId,
//            @PathVariable Long ticketsBooked) {
//
//        inventoryService.updateEventCapacity(eventId, ticketsBooked);
//        return ResponseEntity.ok().build();
//    }
// InventoryService.java - remove the annotations
public void updateEventCapacity(Long eventId, Long ticketsBooked) {
    final Event event = eventRepository.findById(eventId).orElse(null);
    if (event != null) {
        event.setLeftCapacity(event.getLeftCapacity() - ticketsBooked);
        eventRepository.saveAndFlush(event);
    }

}

}