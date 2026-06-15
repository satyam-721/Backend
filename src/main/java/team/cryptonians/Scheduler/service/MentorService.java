package team.cryptonians.Scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import team.cryptonians.Scheduler.dto.AvailabilitySlotResponse;
import team.cryptonians.Scheduler.dto.MentorSummaryResponse;
import team.cryptonians.Scheduler.dto.SlotRequest;
import team.cryptonians.Scheduler.model.AvailabilitySlot;
import team.cryptonians.Scheduler.model.User;
import team.cryptonians.Scheduler.repo.SlotRepo;
import team.cryptonians.Scheduler.repo.UserRepo;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class MentorService {

    @Autowired
    SlotRepo slotRepo;

    @Autowired
    UserRepo userRepo;

    public AvailabilitySlot save(SlotRequest request) {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String mentorName = auth.getName();



        User mentor = userRepo.findByUsername(mentorName);

        ZoneId zoneId = ZoneId.of(mentor.getTimezone());

        ZoneOffset offset =
                zoneId.getRules().getOffset(Instant.now());

        LocalDateTime startTimeUtc = request.startTime().minusSeconds(offset.getTotalSeconds());
        LocalDateTime endTimeUtc = request.endTime().minusSeconds(offset.getTotalSeconds());


        AvailabilitySlot slot = AvailabilitySlot.builder()
                .mentor(mentor)
                .slotType(request.slotType())
                .startTimeUtc(startTimeUtc)
                .endTimeUtc(endTimeUtc)
                .dayOfWeek(request.dayOfWeek())
                .slotDate(request.slotDate())
                .sessionDurationMinutes(request.sessionDurationMinutes())
                .bufferMinutes(request.bufferMinutes())
                .notes(request.notes())
                .is_active(true)
                .createdAt(Instant.now())
                .build();

        return slotRepo.save(slot);

    }



    public List<AvailabilitySlotResponse> getAllSlots() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String mentorname = auth.getName();
        User student = userRepo.findByUsername(mentorname);


        List<AvailabilitySlot> slotList = slotRepo.findAll();
        List<AvailabilitySlotResponse> responseSlotList = new ArrayList<>();

        for (AvailabilitySlot slot : slotList) {

            if (!slot.getMentor().getUsername().equals(mentorname)) continue;

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
}
