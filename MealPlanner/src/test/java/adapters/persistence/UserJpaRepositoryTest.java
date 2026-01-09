package adapters.persistence;

import adapters.Persistence.UserJpaRepository;
import domain.entity.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UserJpaRepositoryTest {

    @Inject
    UserJpaRepository repo;

    @Inject
    EntityManager em;

    private User validUser(String username, String email) {
        return new User(username, email, "password123");
    }

    @Test
    @TestTransaction
    @DisplayName("Speichert einen neuen User und vergibt eine ID")
    void save_persists_new_user_and_sets_id() {
        User saved = repo.save(validUser("alice", "alice@test.com"));

        assertNotNull(saved.getUserId(), "UserId sollte nach save() gesetzt sein");
    }

    @Test
    @TestTransaction
    @DisplayName("Findet einen User anhand der ID")
    void findById_returns_saved_user() {
        User saved = repo.save(validUser("bob", "bob@test.com"));

        Optional<User> found = repo.findById(saved.getUserId());

        assertTrue(found.isPresent());
        assertEquals("bob", found.get().getUsername());
        assertEquals("bob@test.com", found.get().getEmail());
    }

    @Test
    @TestTransaction
    @DisplayName("Findet einen User anhand der Email")
    void findByEmail_returns_matching_user() {
        repo.save(validUser("carol", "carol@test.com"));

        Optional<User> found = repo.findByEmail("carol@test.com");

        assertTrue(found.isPresent());
        assertEquals("carol", found.get().getUsername());
    }

    @Test
    @TestTransaction
    @DisplayName("Findet einen User anhand des Usernames")
    void findByUsername_returns_matching_user() {
        repo.save(validUser("dave", "dave@test.com"));

        Optional<User> found = repo.findByUsername("dave");

        assertTrue(found.isPresent());
        assertEquals("dave@test.com", found.get().getEmail());
    }

    @Test
    @TestTransaction
    @DisplayName("Liefert alle gespeicherten User")
    void findAll_returns_all_users() {
        repo.save(validUser("u1", "u1@test.com"));
        repo.save(validUser("u2", "u2@test.com"));

        assertTrue(repo.findAll().size() >= 2);
    }

    @Test
    @TestTransaction
    @DisplayName("Aktualisiert einen bestehenden User über merge (save mit gesetzter ID)")
    void save_merges_existing_user_and_updates_fields() {
        User saved = repo.save(validUser("erin", "erin@test.com"));
        Long id = saved.getUserId();

        // detached Update-Objekt
        User update = new User("erin_new", "erin_new@test.com", "newpass");
        update.setUserId(id);

        User merged = repo.save(update);

        em.flush();
        em.clear();

        User reloaded = repo.findById(merged.getUserId()).orElseThrow();

        assertEquals(id, reloaded.getUserId());
        assertEquals("erin_new", reloaded.getUsername());
        assertEquals("erin_new@test.com", reloaded.getEmail());
    }

    @Test
    @TestTransaction
    @DisplayName("Wirft Fehler bei doppelter Email (Unique Constraint, falls aktiv)")
    void duplicate_email_should_fail_if_unique_constraint_exists() {
        repo.save(validUser("x1", "dup@test.com"));

        // Zweiter User mit gleicher Email
        assertThrows(RuntimeException.class, () -> repo.save(validUser("x2", "dup@test.com")));
    }

    @Test
    @TestTransaction
    @DisplayName("Wirft Fehler bei doppeltem Username (Unique Constraint, falls aktiv)")
    void duplicate_username_should_fail_if_unique_constraint_exists() {
        repo.save(validUser("dupUser", "a@test.com"));

        // Zweiter User mit gleichem Username
        assertThrows(RuntimeException.class, () -> repo.save(validUser("dupUser", "b@test.com")));
    }
}
