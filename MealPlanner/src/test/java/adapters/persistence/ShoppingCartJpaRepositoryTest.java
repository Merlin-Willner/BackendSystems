package adapters.persistence;

import adapters.Persistence.ShoppingCartJpaRepository;
import domain.entity.ShoppingCart;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ShoppingCartJpaRepositoryTest {

    @Inject
    ShoppingCartJpaRepository repo;

    @Inject
    EntityManager em;

    private ShoppingCart validCart(Long userId) {
        ShoppingCart c = new ShoppingCart();
        c.setUserId(userId);
        return c;
    }

    @Test
    @TestTransaction
    @DisplayName("Speichert einen neuen ShoppingCart und vergibt eine ID")
    void save_persists_new_cart_and_sets_id() {
        ShoppingCart saved = repo.save(validCart(101L));

        em.flush(); // ID bei IDENTITY sicher nach flush
        assertNotNull(saved.getShoppingCartId(), "ShoppingCartId sollte nach save() gesetzt sein");
    }

    @Test
    @TestTransaction
    @DisplayName("Findet einen ShoppingCart anhand der ID")
    void findById_returns_saved_cart() {
        ShoppingCart saved = repo.save(validCart(10L));
        em.flush();

        Optional<ShoppingCart> found = repo.findById(saved.getShoppingCartId());

        assertTrue(found.isPresent());
        assertEquals(10L, found.get().getUserId());
    }

    @Test
    @TestTransaction
    @DisplayName("FindByUserId liefert den ShoppingCart eines Users")
    void findByUserId_returns_matching_cart() {
        repo.save(validCart(99L));
        em.flush();

        Optional<ShoppingCart> found = repo.findByUserId(99L);

        assertTrue(found.isPresent());
        assertEquals(99L, found.get().getUserId());
    }

    @Test
    @TestTransaction
    @DisplayName("FindByUserId liefert Optional.empty wenn kein Cart existiert")
    void findByUserId_returns_empty_if_not_found() {
        Optional<ShoppingCart> found = repo.findByUserId(123456L);
        assertTrue(found.isEmpty());
    }

    @Test
    @TestTransaction
    @DisplayName("save merged einen bestehenden ShoppingCart (Update)")
    void save_merges_existing_cart_and_updates_fields() {
        ShoppingCart saved = repo.save(validCart(102L));
        em.flush();

        Long id = saved.getShoppingCartId();

        em.clear();

        ShoppingCart detached = repo.findById(id).orElseThrow();
        em.detach(detached);

        detached.setUserId(103L);

        repo.save(detached);
        em.flush();
        em.clear();

        ShoppingCart reloaded = repo.findById(id).orElseThrow();
        assertEquals(103L, reloaded.getUserId());
    }


    @Test
    @TestTransaction
    @DisplayName("Wirft Fehler bei zweitem Cart für gleichen User (UNIQUE Constraint)")
    void duplicate_user_cart_should_fail_if_unique_constraint_exists() {
        assertThrows(RuntimeException.class, () -> {
            repo.save(validCart(777L));
            repo.save(validCart(777L));
            em.flush(); // erzwingt den UNIQUE-Check sicher
        });
    }

}
