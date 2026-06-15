package team.cryptonians.Scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import team.cryptonians.Scheduler.dto.AvailabilitySlotResponse;
import team.cryptonians.Scheduler.dto.BookingRequest;
import team.cryptonians.Scheduler.dto.MentorSummaryResponse;
import team.cryptonians.Scheduler.model.AvailabilitySlot;
import team.cryptonians.Scheduler.model.Booking;
import team.cryptonians.Scheduler.model.User;
import team.cryptonians.Scheduler.repo.BookingRepo;
import team.cryptonians.Scheduler.repo.SlotRepo;
import team.cryptonians.Scheduler.repo.UserRepo;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    SlotRepo slotRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    EmailService emailService;

    public List<AvailabilitySlotResponse> getAllSlots() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String studentname = auth.getName();
        User student = userRepo.findByUsername(studentname);


        List<AvailabilitySlot> slotList = slotRepo.findAll();
        List<AvailabilitySlotResponse> responseSlotList = new ArrayList<>();

        for(AvailabilitySlot slot: slotList){
            if(slot.getIs_active() == false){
                Booking booking = bookingRepo.findBySlotId(slot.getId());
                if(booking == null) continue;

                if (! booking.getStudent().getUsername().equals(studentname)) continue;

            }



            ZoneId studentZone = ZoneId.of(student.getTimezone());
            LocalDateTime starttime =
                    slot.getStartTimeUtc()
                            .atZone(ZoneOffset.UTC)      // tell Java this LocalDateTime is UTC
                            .withZoneSameInstant(studentZone)
                            .toLocalDateTime();
            LocalDateTime endtime =
                    slot.getEndTimeUtc()
                            .atZone(ZoneOffset.UTC)      // tell Java this LocalDateTime is UTC
                            .withZoneSameInstant(studentZone)
                            .toLocalDateTime();

            MentorSummaryResponse mentor = new MentorSummaryResponse(
                    slot.getMentor().getUsername(),
                    slot.getMentor().getJobTitle(),
                    slot.getMentor().getCompany()
            );

            responseSlotList.add(
                    new AvailabilitySlotResponse(
                            slot.getId(),
                            slot.getSlotType(),
                            mentor,
                            starttime,
                            endtime,
                            slot.getDayOfWeek(),
                            slot.getSlotDate(),
                            slot.getSessionDurationMinutes(),
                            slot.getBufferMinutes(),
                            slot.getNotes(),
                            slot.getMaxBookableSlots(),
                            slot.getIs_active()

                    )
            );
        }
        return responseSlotList;
    }

    public Booking bookSlot(BookingRequest request) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String studentname = auth.getName();

        AvailabilitySlot slot = slotRepo.findById(request.getSlotId())
                .orElseThrow(()->new RuntimeException("Slot not found"));

        User mentor = userRepo.findByUsername(request.getMentorUsername());
        User student = userRepo.findByUsername(studentname);
        String sessionAgenda = request.getSessionAgenda();

        //converting student local time to UTC
        ZoneId studentZone = ZoneId.of(student.getTimezone());

        LocalDateTime startUtc =
                LocalDateTime.ofInstant(
                        request.getStartTime()
                                .atZone(studentZone)
                                .toInstant(),
                        ZoneOffset.UTC
                );
        LocalDateTime endUtc =
                LocalDateTime.ofInstant(
                        request.getEndTime()
                                .atZone(studentZone)
                                .toInstant(),
                        ZoneOffset.UTC
                );
        Integer durationMinutes =
                Math.toIntExact(Duration.between(startUtc, endUtc).toMinutes());




        Booking booking = new Booking(
                mentor,
                student,
                slot,
                startUtc,
                endUtc,
                durationMinutes,
                Booking.BookingStatus.BOOKED,
                sessionAgenda,
                Instant.now()
        );

        slot.setIs_active(false);
        slotRepo.save(slot);
        booking = bookingRepo.save(booking);
        String meetingLink = new MeetLinkGenerationService().generateFor(booking);
        booking.setMeetLink(meetingLink);

        emailService.sendBookingConfirmation(booking,meetingLink);

        return bookingRepo.save(booking);


    }
}
