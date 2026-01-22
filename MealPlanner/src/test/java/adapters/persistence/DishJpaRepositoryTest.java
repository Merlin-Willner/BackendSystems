package adapters.persistence;

import domain.entity.Dish;
import domain.entity.DishCategory;
import domain.entity.DishIngredient;
import domain.entity.FoodItem;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DishJpaRepositoryTest {

    @Inject
    DishJpaRepository repo;

    @Inject
    EntityManager em;

    /* ---------- Testdaten ---------- */

    private Dish validDish(String name) {
        // Nutze am besten euren Constructor, dann sind wichtige Default-Werte gesetzt
        Dish dish = new Dish(
                1L,                 // userId
                name,
                DishCategory.OTHER, // sicherer Default
                250.0,              // servingWeight
                10,                 // preparationTime
                "img"               // imageUrl (kann auch null sein, falls erlaubt)
        );
        return dish;
    }

    private FoodItem persistValidFoodItem(String name) {
        FoodItem fi = new FoodItem();
        fi.setName(name);
        fi.setBrand("TestBrand");
        fi.setPackSize(1000);
        fi.setPackPrice(2.99);
        fi.setProteinPer100g(10);
        fi.setCarbsPer100g(60);
        fi.setFatPer100g(7);
        fi.setCaloriesPer100g(350);

        em.persist(fi);
        em.flush(); // wichtig, damit fi eine ID bekommt
        return fi;
    }

    private DishIngredient ingredient(Dish dish, FoodItem foodItem, double weight) {
        return new DishIngredient(dish, foodItem, weight);
    }

    /* ---------- Tests ---------- */

    @Test
    @TestTransaction
    @DisplayName("Speichert ein Dish und vergibt eine ID")
    void save_persists_dish_and_sets_id() {
        Dish saved = repo.save(validDish("Pasta"));

        em.flush(); // IDENTITY-ID kommt bei flush sicher
        assertNotNull(saved.getDishId(), "DishId sollte nach dem Speichern gesetzt sein");
    }

    @Test
    @TestTransaction
    @DisplayName("Findet ein Dish anhand der ID")
    void findById_returns_saved_dish() {
        Dish saved = repo.save(validDish("Risotto"));
        em.flush();

        Optional<Dish> found = repo.findById(saved.getDishId());

        assertTrue(found.isPresent());
        assertEquals("Risotto", found.get().getName());
    }

    @Test
    @TestTransaction
    @DisplayName("save merged ein bestehendes Dish (Update)")
    void save_merges_existing_dish() {
        Dish saved = repo.save(validDish("Old Name"));
        em.flush();

        saved.setName("New Name");
        repo.save(saved);

        em.flush();
        em.clear();

        Dish reloaded = repo.findById(saved.getDishId()).orElseThrow();
        assertEquals("New Name", reloaded.getName());
    }

    @Test
    @TestTransaction
    @DisplayName("findAll liefert gespeicherte Dishes")
    void findAll_returns_saved_dishes() {
        repo.save(validDish("Dish 1"));
        repo.save(validDish("Dish 2"));
        em.flush();

        List<Dish> all = repo.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    @TestTransaction
    @DisplayName("findAll lädt Ingredients via LEFT JOIN FETCH")
    void findAll_fetches_ingredients() {
        FoodItem fi1 = persistValidFoodItem("FI-1");
        FoodItem fi2 = persistValidFoodItem("FI-2");

        Dish dish = validDish("Dish with ingredients");
        dish.getIngredients().add(ingredient(dish, fi1, 100));
        dish.getIngredients().add(ingredient(dish, fi2, 50));

        repo.save(dish);
        em.flush();
        em.clear();

        // DishJpaRepository.findAll() nutzt LEFT JOIN FETCH d.ingredients
        List<Dish> all = repo.findAll();

        Dish loaded = all.stream()
                .filter(d -> "Dish with ingredients".equals(d.getName()))
                .findFirst()
                .orElseThrow();

        assertTrue(Hibernate.isInitialized(loaded.getIngredients()),
                "Ingredients sollten durch JOIN FETCH initialisiert sein");
        assertEquals(2, loaded.getIngredients().size());
    }
}
