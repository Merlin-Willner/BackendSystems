package domain.service;

import application.port.in.DishCreationCommand;
import application.port.out.DishRepository;
import application.port.out.FoodItemRepository;
import domain.entity.Dish;
import domain.entity.DishCategory;
import domain.entity.FoodItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishServiceTest {

    @Mock
    DishRepository dishRepository;

    @Mock
    FoodItemRepository foodItemRepository;

    DishService service;

    @BeforeEach
    void setUp() {
        service = new DishService(dishRepository, foodItemRepository);
    }

    @Test
    @DisplayName("create rejects invalid command data")
    void createRejectsInvalidData() {
        // Null command
        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        // Blank name
        assertThrows(IllegalArgumentException.class, () -> service.create(new DishCreationCommand(
                " ", DishCategory.LUNCH, 100, 10, null, 1L, List.of(new DishCreationCommand.IngredientCommand(1L, 50))
        )));
        // Null userId
        assertThrows(IllegalArgumentException.class, () -> service.create(new DishCreationCommand(
                "Dish", DishCategory.LUNCH, 100, 10, null, null, List.of(new DishCreationCommand.IngredientCommand(1L, 50))
        )));
        // Non-positive servingWeight
        assertThrows(IllegalArgumentException.class, () -> service.create(new DishCreationCommand(
                "Dish", DishCategory.LUNCH, 0, 10, null, 1L, List.of(new DishCreationCommand.IngredientCommand(1L, 50))
        )));
        // Missing ingredients
        assertThrows(IllegalArgumentException.class, () -> service.create(new DishCreationCommand(
                "Dish", DishCategory.LUNCH, 100, 10, null, 1L, List.of()
        )));
        // Duplicate ingredient IDs
        FoodItem existing = new FoodItem("Any", "B", 100, 1.0, 10, 10, 5, 100);
        existing.setFoodItemId(1L);
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class, () -> service.create(new DishCreationCommand(
                "Dish", DishCategory.LUNCH, 100, 10, null, 1L, List.of(
                        new DishCreationCommand.IngredientCommand(1L, 50),
                        new DishCreationCommand.IngredientCommand(1L, 60)
                )
        )));
    }

    @Test
    @DisplayName("create loads food items, builds dish, and delegates save")
    void createPersistsDishWithIngredients() {
        FoodItem chicken = new FoodItem("Chicken", "Brand", 1000, 10, 30, 0, 5, 150);
        chicken.setFoodItemId(10L);
        DishCreationCommand command = new DishCreationCommand(
                "Chicken Bowl",
                DishCategory.DINNER,
                400,
                20,
                null,
                5L,
                List.of(new DishCreationCommand.IngredientCommand(10L, 200))
        );

        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(chicken));
        when(dishRepository.save(org.mockito.ArgumentMatchers.any(Dish.class)))
                .thenAnswer(inv -> inv.getArgument(0, Dish.class));

        Dish result = service.create(command);

        ArgumentCaptor<Dish> captor = ArgumentCaptor.forClass(Dish.class);
        verify(dishRepository).save(captor.capture());
        Dish saved = captor.getValue();

        assertEquals("Chicken Bowl", saved.getName());
        assertEquals(1, saved.getIngredients().size(), "Exactly one ingredient attached");
        assertEquals(chicken.getFoodItemId(), saved.getIngredients().get(0).getFoodItemId());
        assertEquals(result, saved, "Service should return the saved dish");
    }

    @Test
    @DisplayName("create fails when food item is missing")
    void createFailsOnMissingFoodItem() {
        DishCreationCommand command = new DishCreationCommand(
                "Dish", DishCategory.LUNCH, 200, 10, null, 2L,
                List.of(new DishCreationCommand.IngredientCommand(999L, 50))
        );
        when(foodItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(jakarta.ws.rs.NotFoundException.class, () -> service.create(command));
    }

    @Test
    @DisplayName("addIngredient validates and saves dish")
    void addIngredientAddsAndSaves() {
        Dish dish = new Dish(1L, "Dish", DishCategory.LUNCH, 300, 10, null);
        dish.setDishId(11L);
        FoodItem item = new FoodItem("Rice", "B", 1000, 2, 7, 78, 1, 360);
        item.setFoodItemId(20L);

        when(dishRepository.findById(11L)).thenReturn(Optional.of(dish));
        when(foodItemRepository.findById(20L)).thenReturn(Optional.of(item));
        when(dishRepository.save(org.mockito.ArgumentMatchers.any(Dish.class)))
                .thenAnswer(inv -> inv.getArgument(0, Dish.class));

        Dish updated = service.addIngredient(11L, 20L, 150);

        assertEquals(1, updated.getIngredients().size());
        assertEquals(20L, updated.getIngredients().get(0).getFoodItemId());
    }

    @Test
    @DisplayName("addIngredient rejects invalid parameters")
    void addIngredientRejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> service.addIngredient(null, 1L, 10));
        assertThrows(IllegalArgumentException.class, () -> service.addIngredient(1L, null, 10));
        assertThrows(IllegalArgumentException.class, () -> service.addIngredient(1L, 1L, 0));
    }

    @Test
    @DisplayName("removeIngredient blocks removal of last ingredient")
    void removeIngredientBlocksLast() {
        Dish dish = new Dish(1L, "Dish", DishCategory.LUNCH, 300, 10, null);
        dish.setDishId(11L);
        FoodItem item = new FoodItem("Rice", "B", 1000, 2, 7, 78, 1, 360);
        item.setFoodItemId(20L);
        dish.addIngredient(item, 100);

        when(dishRepository.findById(11L)).thenReturn(Optional.of(dish));

        assertThrows(IllegalArgumentException.class, () -> service.removeIngredient(11L, 20L));
    }

    @Test
    @DisplayName("updateIngredientWeight updates weight and saves")
    void updateIngredientWeightUpdates() {
        Dish dish = new Dish(1L, "Dish", DishCategory.LUNCH, 300, 10, null);
        dish.setDishId(11L);
        FoodItem item = new FoodItem("Rice", "B", 1000, 2, 7, 78, 1, 360);
        item.setFoodItemId(20L);
        dish.addIngredient(item, 100);

        when(dishRepository.findById(11L)).thenReturn(Optional.of(dish));
        when(dishRepository.save(org.mockito.ArgumentMatchers.any(Dish.class)))
                .thenAnswer(inv -> inv.getArgument(0, Dish.class));

        Dish updated = service.updateIngredientWeight(11L, 20L, 250);

        assertEquals(250, updated.getIngredients().get(0).getWeight());
    }
}
