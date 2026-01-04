package domain.service;

import application.port.out.FoodItemRepository;
import domain.entity.FoodItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodItemServiceTest {

    @Mock
    FoodItemRepository foodItemRepository;

    FoodItemService service;

    @BeforeEach
    void setUp() {
        service = new FoodItemService(foodItemRepository);
    }

    @Test
    @DisplayName("create delegates to repository and returns saved item")
    void createDelegatesToRepository() {
        FoodItem input = new FoodItem("Name", "Brand", 100, 2.5, 10, 20, 5, 150);
        FoodItem persisted = new FoodItem("Name", "Brand", 100, 2.5, 10, 20, 5, 150);
        persisted.setFoodItemId(42L);

        when(foodItemRepository.save(input)).thenReturn(persisted);

        FoodItem result = service.create(input);

        ArgumentCaptor<FoodItem> captor = ArgumentCaptor.forClass(FoodItem.class);
        verify(foodItemRepository).save(captor.capture());
        assertEquals(input, captor.getValue(), "Service should pass the same instance to the repository");
        assertEquals(persisted, result, "Service should return what the repository returns");
    }

    @Test
    @DisplayName("existsByName returns true when repository finds a match")
    void existsByNameTrue() {
        when(foodItemRepository.findByName("Chicken")).thenReturn(Optional.of(new FoodItem()));

        boolean exists = service.existsByName("Chicken");

        assertTrue(exists);
    }

    @Test
    @DisplayName("filterAndRank filters by bounds and sorts by protein desc")
    void filterAndRankFiltersAndSorts() {
        FoodItem lowProtein = new FoodItem("Low", "B", 100, 1.0, 5, 10, 5, 80);
        FoodItem midProtein = new FoodItem("Mid", "B", 100, 1.0, 15, 20, 5, 150);
        FoodItem highProtein = new FoodItem("High", "B", 100, 1.0, 30, 30, 5, 250);

        when(foodItemRepository.findAll()).thenReturn(Arrays.asList(lowProtein, midProtein, highProtein));

        List<FoodItem> result = service.filterAndRank(
                10.0,   // minProtein
                40.0,   // maxProtein
                100.0,  // minCalories
                300.0,  // maxCalories
                null,
                null,
                "protein"
        );

        assertEquals(List.of(highProtein, midProtein), result, "Only items in bounds, sorted by protein desc");
    }

    @Test
    @DisplayName("filterAndRank throws on invalid sort key")
    void filterAndRankThrowsOnInvalidSort() {
        when(foodItemRepository.findAll()).thenReturn(List.of(new FoodItem("Any", "B", 100, 1.0, 10, 10, 10, 100)));

        assertThrows(IllegalArgumentException.class, () ->
                service.filterAndRank(null, null, null, null, null, null, "unknown"));
    }
}
