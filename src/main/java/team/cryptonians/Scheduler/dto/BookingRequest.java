package team.cryptonians.Scheduler.dto;

import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    private Integer slotId;
    private String mentorUsername;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String sessionAgenda;

}
