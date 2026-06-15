package team.cryptonians.Scheduler.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.time.Instant;

@Entity
public class GoogleCredential {

    @Id
    private Long userId;

    private String googleEmail;

    private String googleAccountId;

    @Lob
    private String accessToken;

    @Lob
    private String refreshToken;

    private Instant expiryTime;
}