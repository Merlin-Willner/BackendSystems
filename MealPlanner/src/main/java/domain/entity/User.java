package domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;        // Eindeutige ID
    private String username;    // Eindeutiger Benutzername
    private String email;       // Eindeutige E-Mail
    private String passwordHash;// Passwort (gehashed)
    private UserGoal userGoal;  // Ziel des Users (optional)

    public enum UserGoal {
        BUILD_MUSCLE,
        LOSE_WEIGHT,
        GAIN_WEIGHT,
        HEALTHY_LIFESTYLE
    }

    // Konstruktor
    public User(String username, String email, String passwordHash, UserGoal userGoal) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userGoal = userGoal != null ? userGoal : UserGoal.HEALTHY_LIFESTYLE;
    }

    public User() {
        // JPA requires a no-arg constructor
    }


    // Getter & Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserGoal getUserGoal() { return userGoal; }
    public void setUserGoal(UserGoal userGoal) { this.userGoal = userGoal; }
}
