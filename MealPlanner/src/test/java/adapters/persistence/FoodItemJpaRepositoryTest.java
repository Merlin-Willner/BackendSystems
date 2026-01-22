package adapters.persistence;

import domain.entity.FoodItem;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class FoodItemJpaRepositoryTest {

    @Inject
    FoodItemJpaRepository repo;

    @Inject
    EntityManager em;

    private FoodItem validFoodItem(String name) {
        FoodItem fi = new FoodItem();
        fi.setName(name);
        fi.setBrand("TestBrand");
        fi.setPackSize(1000);
        fi.setPackPrice(2.5);
        fi.setProteinPer100g(10);
        fi.setCarbsPer100g(60);
        fi.setFatPer100g(7);
        fi.setCaloriesPer100g(350);
        return fi;
    }

    @Test
    @DisplayName("Should persist FoodItem and assign ID")
    @TestTransaction
    void save_persists_entity_and_sets_id() {
        FoodItem saved = repo.save(validFoodItem("Oats"));

        assertThat(saved.getFoodItemId()).isNotNull();
    }

    @Test
    @DisplayName("Should find FoodItem by ID")
    @TestTransaction
    void findById_returns_saved_entity() {
        FoodItem saved = repo.save(validFoodItem("Rice"));

        FoodItem found = repo.findById(saved.getFoodItemId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Rice");
    }

    @Test
    @DisplayName("Should return all FoodItems")
    @TestTransaction
    void findAll_returns_all_entities() {
        repo.save(validFoodItem("A"));
        repo.save(validFoodItem("B"));

        assertThat(repo.findAll())
                .extracting(FoodItem::getName)
                .contains("A", "B");
    }

    @Test
    @DisplayName("Should find FoodItem by name")
    @TestTransaction
    void findByName_returns_matching_entity() {
        repo.save(validFoodItem("Pasta"));

        var result = repo.findByName("Pasta");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Pasta");
    }

    @Test
    @DisplayName("Should fail on duplicate FoodItem name")
    @TestTransaction
    void duplicate_name_throws_exception_if_unique_constraint_exists() {
        repo.save(validFoodItem("Unique"));

        assertThatThrownBy(() -> repo.save(validFoodItem("Unique")))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
