package team.cryptonians.Scheduler.dto;

import team.cryptonians.Scheduler.model.AvailabilitySlot;

import java.time.*;

public record SlotRequest(
    AvailabilitySlot.SlotType slotType,

    LocalDateTime startTime,
    LocalDateTime endTime,

    DayOfWeek dayOfWeek,
    LocalDate slotDate,

    Integer sessionDurationMinutes,
    Integer bufferMinutes,

    String notes


) {
}
