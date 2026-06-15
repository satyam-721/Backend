package team.cryptonians.Scheduler.service;

import org.springframework.stereotype.Service;
import team.cryptonians.Scheduler.model.Booking;

import java.security.SecureRandom;

@Service
public class MeetLinkGenerationService {
    private static final String BASE_URL    = "https://meet.jit.si/";
    private static final String CHARS       = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int    CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateFor(Booking booking) {
        if (booking.getId() == null) {
            throw new IllegalStateException(
                    "Booking must be saved before generating a meet link — ID is null"
            );
        }

        String randomCode = generateRandomCode(CODE_LENGTH);
        return BASE_URL + "mentorship-" + booking.getId() + "-" + randomCode;
    }


    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public String regenerateFor(Booking booking) {
        return generateFor(booking);
    }
}
