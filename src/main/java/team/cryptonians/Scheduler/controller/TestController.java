package team.cryptonians.Scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team.cryptonians.Scheduler.model.Booking;
import team.cryptonians.Scheduler.repo.BookingRepo;
import team.cryptonians.Scheduler.service.EmailService;

@RestController
public class TestController {


    @GetMapping("hello")
    public String greet(){
        return "HelloWorld";
    }

    @PostMapping("post")
    public String postgreet(){
        return "HELLO POST";
    }
    @Autowired
    private EmailService emailService;

    @Autowired
    private BookingRepo bookingRepo;

    @GetMapping("/email")
    public String testEmail() {

        Booking booking =
                bookingRepo.findById(3).orElseThrow();

        emailService.sendBookingConfirmation(
                booking,
                "https://meet.google.com/test-room"
        );

        return "Email sent";
    }
}
