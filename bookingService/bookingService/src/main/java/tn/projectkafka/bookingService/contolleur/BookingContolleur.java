package tn.projectkafka.bookingService.contolleur;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.projectkafka.bookingService.request.BookingRequest;
import tn.projectkafka.bookingService.response.BookingResponse;
import tn.projectkafka.bookingService.service.BookingService;

@RestController
@RequestMapping("/api/v1")
public class BookingContolleur {

    private final BookingService bookingService;

    @Autowired
    public BookingContolleur(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping(consumes="application/json",produces="application/json",path="/booking")
    public BookingResponse createBooking(@RequestBody BookingRequest request){
        return bookingService.createBooking(request);
    }

}
