package team.cryptonians.Scheduler.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import team.cryptonians.Scheduler.model.Booking;
import team.cryptonians.Scheduler.model.User;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;



@Service
public class EmailService {

    @Autowired
    IcsGeneratorService icsGeneratorService;

    @Autowired
    JavaMailSender mailSender;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy 'at' hh:mm a z");

    private static final String FROM_EMAIL = "satyamsagar305@gmail.com";
    private static final String FROM_NAME  = "MentorSync Scheduler";


    @Async
    public void sendBookingConfirmation(Booking booking, String meetLink) {
        System.out.println("Sending confirmation emails for booking "+ booking.getId());

        // Generate shared assets once
        String icsContent        = icsGeneratorService.generateIcsContent(booking, meetLink);
        String googleCalendarUrl = icsGeneratorService.generateGoogleCalendarUrl(booking, meetLink);

        // Send to student (time shown in student's timezone)
        sendToStudent(booking, meetLink, icsContent, googleCalendarUrl);

        // Send to mentor (time shown in mentor's timezone)
        sendToMentor(booking, meetLink, icsContent, googleCalendarUrl);

        System.out.println("Confirmation emails sent for booking "+ booking.getId());
    }


    // ─────────────────────────────────────────────
    // Email to student
    // ─────────────────────────────────────────────

    private void sendToStudent(Booking booking, String meetLink,
                               String icsContent, String googleCalendarUrl) {
        User student      = booking.getStudent();
        User   mentor       = booking.getMentor();
        String sessionTime  = formatTime(booking, student.getTimezone());
        String agenda       = booking.getSessionAgenda() != null
                ? booking.getSessionAgenda()
                : "Not specified";

        String subject = "✅ Your session with " + mentor.getUsername() + " is confirmed";

        String body = buildStudentEmailHtml(
                student.getUsername(),
                mentor,
                sessionTime,
                agenda,
                meetLink,
                googleCalendarUrl,
                booking.getId()
        );

        sendEmail(student.getEmail(), subject, body, icsContent, "session.ics");
    }




    // ─────────────────────────────────────────────
    // Email to mentor
    // ─────────────────────────────────────────────

    private void sendToMentor(Booking booking, String meetLink,
                              String icsContent, String googleCalendarUrl) {
        User   mentor      = booking.getMentor();
        User   student     = booking.getStudent();
        String sessionTime = formatTime(booking, mentor.getTimezone());
        String agenda      = booking.getSessionAgenda() != null
                ? booking.getSessionAgenda()
                : "Not specified";

        String subject = "📅 New session booked — "
                + student.getUsername() + ", "
                + formatDateOnly(booking, mentor.getTimezone());

        String body = buildMentorEmailHtml(
                mentor.getUsername(),
                student,
                sessionTime,
                agenda,
                meetLink,
                googleCalendarUrl,
                booking.getId()
        );

        sendEmail(mentor.getEmail(), subject, body, icsContent, "session.ics");
    }

    private void sendEmail(String to, String subject, String htmlBody,
                           String icsContent, String icsFilename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);   // true = HTML

            // Attach .ics file
            // Content type "text/calendar" is what triggers calendar apps to recognize it
            helper.addAttachment(
                    icsFilename,
                    new ByteArrayResource(icsContent.getBytes()),
                    "text/calendar; charset=UTF-8; method=REQUEST"
            );

            mailSender.send(message);
            System.out.println("Email sent to "+to+": " + subject);

        } catch (Exception e) {
            System.out.println("Failed to send email to "+to+": " + e.getMessage());
            System.out.println(e);
            e.printStackTrace();
            // Don't throw — a failed email should not rollback the booking
        }
    }
    private String formatTime(Booking booking, String timezone) {
        ZonedDateTime zdt = booking.getStartUtc().atZone(ZoneId.of(timezone));
        ZonedDateTime end = booking.getEndUtc().atZone(ZoneId.of(timezone));
        return DISPLAY_FORMAT.format(zdt)
                + " – "
                + DateTimeFormatter.ofPattern("hh:mm a z").format(end);
    }

    private String formatDateOnly(Booking booking, String timezone) {
        ZonedDateTime zdt = booking.getStartUtc().atZone(ZoneId.of(timezone));
        return DateTimeFormatter.ofPattern("EEE dd MMM").format(zdt);
    }

    //htmls

    private String baseTemplate(String title, String content) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8">
            <style>
              * {
                  box-sizing: border-box;
              }
            
              body {
                  margin: 0;
                  padding: 40px 20px;
                  background: #0f172a;
                  font-family: 'Segoe UI', Arial, sans-serif;
                  color: #e2e8f0;
                  line-height: 1.6;
              }
            
              .wrapper {
                  max-width: 620px;
                  margin: auto;
                  background: #111827;
                  border: 1px solid #1f2937;
                  border-radius: 20px;
                  overflow: hidden;
                  box-shadow:
                      0 20px 40px rgba(0,0,0,0.35),
                      0 0 0 1px rgba(255,255,255,0.03);
              }
            
              .header {
                  padding: 36px;
                  background:
                      linear-gradient(
                          135deg,
                          #16a34a 0%%,
                          #22c55e 50%%,
                          #4ade80 100%%
                      );
              }
            
              .header h1 {
                  margin: 0;
                  color: white;
                  font-size: 24px;
                  font-weight: 700;
                  letter-spacing: -0.5px;
              }
            
              .header p {
                  margin: 8px 0 0;
                  color: rgba(255,255,255,0.9);
                  font-size: 14px;
              }
            
              .body {
                  padding: 36px;
              }
            
              .body p {
                  margin: 0 0 18px;
                  color: #cbd5e1;
                  font-size: 15px;
              }
            
              .card {
                  margin: 28px 0;
                  padding: 24px;
                  background: #0f172a;
                  border: 1px solid #1e293b;
                  border-radius: 14px;
              }
            
              .card h3 {
                  margin: 0 0 18px;
                  color: #4ade80;
                  font-size: 13px;
                  font-weight: 700;
                  letter-spacing: 1.2px;
                  text-transform: uppercase;
              }
            
              table {
                  width: 100%%;
                  border-collapse: collapse;
              }
            
              td {
                  padding: 10px 0;
                  font-size: 14px;
                  border-bottom: 1px solid rgba(255,255,255,0.05);
              }
            
              tr:last-child td {
                  border-bottom: none;
              }
            
              td:first-child {
                  width: 140px;
                  color: #94a3b8;
                  font-weight: 500;
              }
            
              td:last-child {
                  color: #f8fafc;
                  font-weight: 600;
              }
            
              .actions {
                  margin-top: 30px;
                  text-align: center;
              }
            
              .btn-primary {
                  display: inline-block;
                  background: #22c55e;
                  color: white !important;
                  text-decoration: none;
                  padding: 14px 30px;
                  border-radius: 12px;
                  font-weight: 600;
                  font-size: 15px;
                  box-shadow: 0 8px 20px rgba(34,197,94,0.3);
              }
            
              .btn-secondary {
                  display: inline-block;
                  margin-top: 12px;
                  text-decoration: none;
                  color: #4ade80 !important;
                  font-size: 14px;
              }
            
              .divider {
                  height: 1px;
                  background: #1f2937;
                  margin: 28px 0;
              }
            
              .muted {
                  color: #94a3b8;
                  font-size: 13px;
              }
            
              .footer {
                  padding: 24px;
                  text-align: center;
                  background: #0b1220;
                  border-top: 1px solid #1f2937;
              }
            
              .footer p {
                  margin: 0;
                  color: #64748b;
                  font-size: 12px;
              }
            
              @media only screen and (max-width: 600px) {
                  .header,
                  .body,
                  .footer {
                      padding: 24px;
                  }
            
                  td:first-child {
                      width: 110px;
                  }
              }
            </style>
            </head>
            <body>
            <div class="wrapper">
                <div class="header">
                    <h1>Mentorship Scheduler</h1>
                    <p>%s</p>
                </div>
            
                <div class="body">
                    %s
                </div>
            
                <div class="footer">
                    <p>
                        © 2026 Mentorship Scheduler • Helping mentors and students connect.
                    </p>
                </div>
            </div>
            </body>
            </html>
            """.formatted(title, content);

    }


    private String buildMentorEmailHtml(String mentorFirstName, User student,
                                        String sessionTime, String agenda,
                                        String meetLink, String googleCalendarUrl,
                                        Integer bookingId) {
        return baseTemplate(
                "New session booked 📅",
                """
                <p>Hi <strong>%s</strong>,</p>
                <p>A student has booked a session with you.</p>
     
                <div class="card">
                  <h3>Session Details</h3>
                  <table>
                    <tr><td>Student</td>    <td><strong>%s</strong>%s</td></tr>
                    <tr><td>Date & Time</td><td><strong>%s</strong></td></tr>
                    <tr><td>Their Agenda</td><td><em>"%s"</em></td></tr>
                  </table>
                </div>
     
                <p style="text-align:center; margin: 28px 0">
                  <a href="%s" class="btn-primary">Join Meeting ↗</a>
                </p>
     
                <div class="card" style="text-align:center">
                  <p style="margin:0 0 12px">Add this session to your calendar</p>
                  <a href="%s" class="btn-secondary">Add to Google Calendar</a>
                  <p class="muted" style="margin:12px 0 0">
                    Or open the attached <strong>session.ics</strong> file
                  </p>
                </div>
     
                <p class="muted" style="text-align:center; margin-top:24px">Booking ID: #%d</p>
                """.formatted(
                        mentorFirstName,
                        student.getUsername(),
                        student.getGithubUsername() != null
                                ? "<br><span class=\"muted\">github.com/" + student.getGithubUsername() + "</span>"
                                : "",
                        sessionTime,
                        agenda,
                        meetLink,
                        googleCalendarUrl,
                        bookingId
                )
        );
    }

    private String buildStudentEmailHtml(String studentFirstName, User mentor,
                                         String sessionTime, String agenda,
                                         String meetLink, String googleCalendarUrl,
                                         Integer bookingId) {
        return baseTemplate(
                "Your session is confirmed ✅",
                """
                <p>Hi <strong>%s</strong>,</p>
                <p>Your mentorship session has been booked successfully.</p>
     
                <div class="card">
                  <h3>Session Details</h3>
                  <table>
                    <tr><td>Mentor</td>    <td><strong>%s</strong><br><span class="muted">%s</span></td></tr>
                    <tr><td>Date & Time</td><td><strong>%s</strong></td></tr>
                    <tr><td>Your Agenda</td><td>%s</td></tr>
                  </table>
                </div>
     
                <p style="text-align:center; margin: 28px 0">
                  <a href="%s" class="btn-primary">Join Meeting ↗</a>
                </p>
     
                <div class="card" style="text-align:center">
                  <p style="margin:0 0 12px">Add this session to your calendar</p>
                  <a href="%s" class="btn-secondary">Add to Google Calendar</a>
                  <p class="muted" style="margin:12px 0 0">
                    Or open the attached <strong>session.ics</strong> file to add to Outlook or Apple Calendar
                  </p>
                </div>
     
                <p class="muted" style="text-align:center; margin-top:24px">
                  Need to cancel? You can cancel up to 2 hours before the session
                  from your <a href="http://localhost:3000/dashboard">dashboard</a>.
                  <br>Booking ID: #%d
                </p>
                """.formatted(
                        studentFirstName,
                        mentor.getUsername(),
                        mentor.getJobTitle() != null ? mentor.getJobTitle() : "",
                        sessionTime,
                        agenda,
                        meetLink,
                        googleCalendarUrl,
                        bookingId
                )
        );
    }




}
