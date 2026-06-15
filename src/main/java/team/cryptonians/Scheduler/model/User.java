package team.cryptonians.Scheduler.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="users")
//@EventListener(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "role", nullable = false, length = 10)
    private String role;                   //MENTOR , STUDENT



    //---------
    //Timezone
    //----------
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    //---------------
    //Profile Info (mentor focus, nullable for students)
    //--------------

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_username", length = 50)
    private String githubUsername;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;            // For mentor ONLY

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "company", length = 100)
    private String company;
}
