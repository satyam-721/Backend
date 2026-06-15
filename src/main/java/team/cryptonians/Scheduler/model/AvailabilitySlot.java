package team.cryptonians.Scheduler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;


/**
 * Represents a recurring or one-time availability window set by a MENTOR.
 *
 * Two slot types:
 *   RECURRING  — repeats every week on a given DayOfWeek (e.g. every Monday 4–6 PM UTC)
 *   ONE_TIME   — a single specific date window (slotDate must be set)
 *
 * All times are stored in UTC. Conversion to the student's local timezone
 * happens in TimezoneService before sending to the frontend.
 */


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="availability_slots")
public class AvailabilitySlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private SlotType slotType;    //RECURRING,ONE_TIME

    public enum SlotType{
        RECURRING,
        ONE_TIME
    }


    //RelationShip
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    //Time Window (Stored in UTC) [for onetime or weekly(Recurring)]
    @Column(name = "start_time_utc", nullable = false)
    private LocalDateTime startTimeUtc;

    @Column(name = "end_time_utc", nullable = false)
    private LocalDateTime endTimeUtc;

    //Recurring Fields (null for slotType=ONE_TYPE)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    //ONE_TIME fields
    private LocalDate slotDate;



    private Integer sessionDurationMinutes;

    private Integer bufferMinutes;

    private String notes;

    private Boolean is_active = true;

    private Instant createdAt;

    //Helper Methods
    public Long getWindowDurationMinutes(){
        return Duration.between(startTimeUtc,endTimeUtc).toMinutes();
    }

    public int getMaxBookableSlots(){
        long window = getWindowDurationMinutes();
        int slot = sessionDurationMinutes + bufferMinutes;
        return (int) (window/slot);
    }

    public boolean isRecurring() {
        return SlotType.RECURRING.equals(this.slotType);
    }

    public boolean isOneTime() {
        return SlotType.ONE_TIME.equals(this.slotType);
    }



    // TEST


    public void validate() {
        if (slotType == SlotType.RECURRING && dayOfWeek == null) {
            throw new IllegalStateException("dayOfWeek is required for RECURRING slots");
        }
        if (slotType == SlotType.ONE_TIME && slotDate == null) {
            throw new IllegalStateException("slotDate is required for ONE_TIME slots");
        }
        if (!endTimeUtc.isAfter(startTimeUtc)) {
            throw new IllegalStateException("endTimeUtc must be after startTimeUtc");
        }
    }



}
