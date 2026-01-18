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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    @DisplayName("findById returns item when exists")
    void findByIdReturnsItem() {
        FoodItem item = new FoodItem("Name", "Brand", 100, 2.5, 10, 20, 5, 150);
        item.setFoodItemId(1L);

        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(item));

        FoodItem result = service.findById(1L);

        assertEquals(item, result);
    }

    @Test
    @DisplayName("findById returns null when not found")
    void findByIdReturnsNullWhenNotFound() {
        when(foodItemRepository.findById(999L)).thenReturn(Optional.empty());

        FoodItem result = service.findById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("findAll delegates to repository")
    void findAllDelegatesToRepository() {
        List<FoodItem> items = List.of(new FoodItem("A", "B", 100, 1.0, 10, 10, 10, 100));
        when(foodItemRepository.findAll()).thenReturn(items);

        List<FoodItem> result = service.findAll();

        assertEquals(items, result);
        verify(foodItemRepository).findAll();
    }

    @Test
    @DisplayName("update modifies existing item")
    void updateModifiesItem() {
        FoodItem existing = new FoodItem("Old", "Brand", 100, 1.0, 10, 20, 5, 150);
        existing.setFoodItemId(1L);

        FoodItem updated = new FoodItem("New", "NewBrand", 200, 2.0, 15, 25, 10, 200);

        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(foodItemRepository.findByName("New")).thenReturn(Optional.empty());
        when(foodItemRepository.save(existing)).thenReturn(existing);

        FoodItem result = service.update(1L, updated);

        assertEquals("New", result.getName());
        assertEquals("NewBrand", result.getBrand());
        verify(foodItemRepository).save(existing);
    }

    @Test
    @DisplayName("update returns null when item not found")
    void updateReturnsNullWhenNotFound() {
        FoodItem updated = new FoodItem("Name", "Brand", 100, 1.0, 10, 20, 5, 150);

        when(foodItemRepository.findById(999L)).thenReturn(Optional.empty());

        FoodItem result = service.update(999L, updated);

        assertNull(result);
    }

    @Test
    @DisplayName("update throws 409 when name already exists")
    void updateThrowsOnDuplicateName() {
        FoodItem existing = new FoodItem("Old", "Brand", 100, 1.0, 10, 20, 5, 150);
        existing.setFoodItemId(1L);

        FoodItem other = new FoodItem("Taken", "B", 100, 1.0, 10, 20, 5, 150);
        other.setFoodItemId(2L);

        FoodItem updated = new FoodItem("Taken", "Brand", 100, 1.0, 10, 20, 5, 150);

        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(foodItemRepository.findByName("Taken")).thenReturn(Optional.of(other));

        assertThrows(jakarta.ws.rs.WebApplicationException.class, () -> service.update(1L, updated));
    }

    @Test
    @DisplayName("delete removes existing item")
    void deleteRemovesItem() {
        FoodItem item = new FoodItem("Name", "Brand", 100, 1.0, 10, 20, 5, 150);
        item.setFoodItemId(1L);

        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(item));

        boolean result = service.delete(1L);

        assertTrue(result);
        verify(foodItemRepository).delete(item);
    }

    @Test
    @DisplayName("delete returns false when item not found")
    void deleteReturnsFalseWhenNotFound() {
        when(foodItemRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = service.delete(999L);

        assertFalse(result);
    }
}
