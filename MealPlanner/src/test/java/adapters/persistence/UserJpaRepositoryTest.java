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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UserJpaRepositoryTest {

    @Inject
    UserJpaRepository repo;

    @Inject
    EntityManager em;

    private String uniqueName() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private User validUser(String username, String email) {
        return new User(username, email, "password123");
    }

    @Test
    @TestTransaction
    @DisplayName("Speichert einen neuen User und vergibt eine ID")
    void save_persists_new_user_and_sets_id() {
        String name = uniqueName();
        User saved = repo.save(validUser(name, name + "@test.com"));

        assertNotNull(saved.getUserId(), "UserId sollte nach save() gesetzt sein");
    }

    @Test
    @TestTransaction
    @DisplayName("Findet einen User anhand der ID")
    void findById_returns_saved_user() {
        String name = uniqueName();
        User saved = repo.save(validUser(name, name + "@test.com"));

        Optional<User> found = repo.findById(saved.getUserId());

        assertTrue(found.isPresent());
        assertEquals(name, found.get().getUsername());
        assertEquals(name + "@test.com", found.get().getEmail());
    }

    @Test
    @TestTransaction
    @DisplayName("Findet einen User anhand der Email")
    void findByEmail_returns_matching_user() {
        String name = uniqueName();
        repo.save(validUser(name, name + "@test.com"));

        Optional<User> found = repo.findByEmail(name + "@test.com");

        assertTrue(found.isPresent());
        assertEquals(name, found.get().getUsername());
    }

    @Test
    @TestTransaction
    @DisplayName("Findet einen User anhand des Usernames")
    void findByUsername_returns_matching_user() {
        String name = uniqueName();
        repo.save(validUser(name, name + "@test.com"));

        Optional<User> found = repo.findByUsername(name);

        assertTrue(found.isPresent());
        assertEquals(name + "@test.com", found.get().getEmail());
    }

    @Test
    @TestTransaction
    @DisplayName("Liefert alle gespeicherten User")
    void findAll_returns_all_users() {
        String n1 = uniqueName();
        String n2 = uniqueName();
        repo.save(validUser(n1, n1 + "@test.com"));
        repo.save(validUser(n2, n2 + "@test.com"));

        assertTrue(repo.findAll().size() >= 2);
    }

    @Test
    @TestTransaction
    @DisplayName("Aktualisiert einen bestehenden User über merge (save mit gesetzter ID)")
    void save_merges_existing_user_and_updates_fields() {
        String name = uniqueName();
        User saved = repo.save(validUser(name, name + "@test.com"));
        em.flush();

        String newName = uniqueName();
        saved.setUsername(newName);
        saved.setEmail(newName + "@test.com");
        saved.setPasswordHash("newpass");

        User merged = repo.save(saved);

        em.flush();
        em.clear();

        User reloaded = repo.findById(merged.getUserId()).orElseThrow();

        assertEquals(saved.getUserId(), reloaded.getUserId());
        assertEquals(newName, reloaded.getUsername());
        assertEquals(newName + "@test.com", reloaded.getEmail());
    }

    @Test
    @TestTransaction
    @DisplayName("Wirft Fehler bei doppelter Email (Unique Constraint, falls aktiv)")
    void duplicate_email_should_fail_if_unique_constraint_exists() {
        String email = uniqueName() + "@dup.com";
        repo.save(validUser(uniqueName(), email));

        // Zweiter User mit gleicher Email
        assertThrows(RuntimeException.class, () -> repo.save(validUser(uniqueName(), email)));
    }

    @Test
    @TestTransaction
    @DisplayName("Wirft Fehler bei doppeltem Username (Unique Constraint, falls aktiv)")
    void duplicate_username_should_fail_if_unique_constraint_exists() {
        String dupName = uniqueName();
        repo.save(validUser(dupName, uniqueName() + "@a.com"));

        // Zweiter User mit gleichem Username
        assertThrows(RuntimeException.class, () -> repo.save(validUser(dupName, uniqueName() + "@b.com")));
    }
}
