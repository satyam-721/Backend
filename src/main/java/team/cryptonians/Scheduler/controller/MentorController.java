package team.cryptonians.Scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.cryptonians.Scheduler.dto.AvailabilitySlotResponse;
import team.cryptonians.Scheduler.dto.SlotRequest;
import team.cryptonians.Scheduler.model.AvailabilitySlot;
import team.cryptonians.Scheduler.service.MentorService;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@CrossOrigin("*")
public class MentorController {

    @Autowired
    MentorService slotService;

    @GetMapping("greet")
    public String greet(){
        return "Hello Mentor";
    }

    @PostMapping("slot")
    public AvailabilitySlot saveSlot(@RequestBody SlotRequest slot){

        AvailabilitySlot savedSlot = slotService.save(slot);

        return savedSlot;
    }

    @GetMapping("slots")
    public List<AvailabilitySlotResponse> getSlots(){
        return slotService.getAllSlots();
    }




}
