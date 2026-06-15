package team.cryptonians.Scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.cryptonians.Scheduler.dto.AvailabilitySlotResponse;
import team.cryptonians.Scheduler.dto.BookingRequest;
import team.cryptonians.Scheduler.model.Booking;
import team.cryptonians.Scheduler.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin("*")
public class StudentController {

    @Autowired
    StudentService studentService;

    @GetMapping("mentorslots")
    public List<AvailabilitySlotResponse> getSlots(){
        return studentService.getAllSlots();
    }

    @PostMapping("bookslot")
    public ResponseEntity<?> bookSlot(@RequestBody BookingRequest request){
        Booking booking = studentService.bookSlot(request);

        if(booking==null){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }



}
