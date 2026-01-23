package domain.entity;

public class User {

    private Long userId;

    private String username;

    private String email;

    private String passwordHash;

    private Long version;

    public User(String username, String email, String passwordHash) {
        setUsername(username);
        setEmail(email);
        setPasswordHash(passwordHash);
    }

    public User() {
        // JPA requires a no-arg constructor
    }

    // Getter & Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username darf nicht leer sein");
        this.username = username;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email darf nicht leer sein");
        this.email = email;
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) {
        if(passwordHash == null || passwordHash.isBlank())
            throw new IllegalArgumentException("Passwort darf nicht leer sein");
        this.passwordHash = passwordHash;
    }
}
