package team.cryptonians.Scheduler.dto;

import team.cryptonians.Scheduler.model.AvailabilitySlot;

import java.time.*;


public record AvailabilitySlotResponse(
        Integer id,
        AvailabilitySlot.SlotType slotType,
        MentorSummaryResponse mentor,
        LocalDateTime startTime,
        LocalDateTime endTime,
        DayOfWeek dayOfWeek,
        LocalDate slotDate,
        Integer sessionDurationMinutes,
        Integer bufferMinutes,
        String notes,
        Integer maxBookableSlots,
        Boolean is_active
) {

}