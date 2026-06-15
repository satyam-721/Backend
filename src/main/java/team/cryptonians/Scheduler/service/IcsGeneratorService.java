package team.cryptonians.Scheduler.service;

import org.springframework.stereotype.Service;
import team.cryptonians.Scheduler.model.Booking;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class IcsGeneratorService {
    private static final DateTimeFormatter ICS_FORMAT =
            DateTimeFormatter
                    .ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneOffset.UTC);

    public String generateIcsContent(Booking booking, String meetLink){
        String mentorName  = booking.getMentor().getUsername();
        String studentName = booking.getStudent().getUsername();
        String agenda      = booking.getSessionAgenda() != null
                ? booking.getSessionAgenda()
                : "No agenda specified";

        // Escape special characters in .ics format
        // Commas and semicolons must be escaped, newlines use \n
        String description = escapeIcs(
                "Agenda: " + agenda + "\n" +
                        "Join Meeting: " + meetLink + "\n" +
                        "Booking ID: #" + booking.getId()
        );

        return "BEGIN:VCALENDAR\r\n"
                + "VERSION:2.0\r\n"
                + "PRODID:-//Mentorship Scheduler//EN\r\n"
                + "CALSCALE:GREGORIAN\r\n"
                + "METHOD:REQUEST\r\n"                         // treats this as an invite
                + "BEGIN:VEVENT\r\n"
                + "UID:booking-" + booking.getId() + "@mentorship.com\r\n"
                + "DTSTAMP:"     + ICS_FORMAT.format(Instant.now())               + "\r\n"
                + "DTSTART:"     + ICS_FORMAT.format(booking.getStartUtc())       + "\r\n"
                + "DTEND:"       + ICS_FORMAT.format(booking.getEndUtc())         + "\r\n"
                + "SUMMARY:Mentorship Session — " + mentorName + " & " + studentName + "\r\n"
                + "DESCRIPTION:" + description                                    + "\r\n"
                + "LOCATION:"    + meetLink                                       + "\r\n"
                + "ORGANIZER;CN=Mentorship Scheduler:mailto:noreply@mentorship.com\r\n"
                + "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;RSVP=TRUE;"
                + "CN=" + mentorName  + ":mailto:" + booking.getMentor().getEmail()  + "\r\n"
                + "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;RSVP=TRUE;"
                + "CN=" + studentName + ":mailto:" + booking.getStudent().getEmail() + "\r\n"
                + "STATUS:CONFIRMED\r\n"
                + "SEQUENCE:0\r\n"                            // increment this on updates
                + "BEGIN:VALARM\r\n"                          // reminder 30 min before
                + "TRIGGER:-PT30M\r\n"
                + "ACTION:DISPLAY\r\n"
                + "DESCRIPTION:Your mentorship session starts in 30 minutes\r\n"
                + "END:VALARM\r\n"
                + "BEGIN:VALARM\r\n"                          // reminder 24 hrs before
                + "TRIGGER:-PT24H\r\n"
                + "ACTION:EMAIL\r\n"
                + "DESCRIPTION:Your mentorship session is tomorrow\r\n"
                + "END:VALARM\r\n"
                + "END:VEVENT\r\n"
                + "END:VCALENDAR\r\n";
    }

    public String generateGoogleCalendarUrl(Booking booking, String meetLink) {
        String mentorName  = booking.getMentor().getUsername();
        String studentName = booking.getStudent().getUsername();
        String agenda      = booking.getSessionAgenda() != null
                ? booking.getSessionAgenda()
                : "No agenda specified";

        String title = "Mentorship Session — " + mentorName + " & " + studentName;

        String details = "Agenda: " + agenda + "\n"
                + "Join Meeting: " + meetLink + "\n"
                + "Booking ID: #" + booking.getId();

        // Google Calendar date format: YYYYMMDDTHHmmssZ/YYYYMMDDTHHmmssZ
        String dates = ICS_FORMAT.format(booking.getStartUtc())
                + "/"
                + ICS_FORMAT.format(booking.getEndUtc());

        return "https://calendar.google.com/calendar/render"
                + "?action=TEMPLATE"
                + "&text="     + encode(title)
                + "&dates="    + dates                                  // no encoding — slashes needed
                + "&details="  + encode(details)
                + "&location=" + encode(meetLink)
                + "&add="      + encode(booking.getMentor().getEmail())  // pre-adds attendees
                + "&add="      + encode(booking.getStudent().getEmail());
    }

    public String generateCancellationIcs(Booking booking) {
        String mentorName  = booking.getMentor().getUsername();
        String studentName = booking.getStudent().getUsername();

        return "BEGIN:VCALENDAR\r\n"
                + "VERSION:2.0\r\n"
                + "PRODID:-//Mentorship Scheduler//EN\r\n"
                + "CALSCALE:GREGORIAN\r\n"
                + "METHOD:CANCEL\r\n"                         // ← cancellation signal
                + "BEGIN:VEVENT\r\n"
                + "UID:booking-" + booking.getId() + "@mentorship.com\r\n"  // same UID as original
                + "DTSTAMP:"  + ICS_FORMAT.format(Instant.now())            + "\r\n"
                + "DTSTART:"  + ICS_FORMAT.format(booking.getStartUtc())    + "\r\n"
                + "DTEND:"    + ICS_FORMAT.format(booking.getEndUtc())      + "\r\n"
                + "SUMMARY:CANCELLED — Mentorship Session: " + mentorName + " & " + studentName + "\r\n"
                + "ORGANIZER;CN=Mentorship Scheduler:mailto:noreply@mentorship.com\r\n"
                + "ATTENDEE:mailto:" + booking.getMentor().getEmail()  + "\r\n"
                + "ATTENDEE:mailto:" + booking.getStudent().getEmail() + "\r\n"
                + "STATUS:CANCELLED\r\n"
                + "SEQUENCE:1\r\n"                            // ← higher than original (0)
                + "END:VEVENT\r\n"
                + "END:VCALENDAR\r\n";
    }
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String escapeIcs(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace(";",  "\\;")
                .replace(",",  "\\,")
                .replace("\n", "\\n");
    }




}
