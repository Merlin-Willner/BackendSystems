package application.port.in;

import domain.entity.Dish;

import java.util.List;

public interface DishAPI {
    Dish create(DishCreationCommand command);

    List<Dish> findAll();

    Dish findById(Long id);

    Dish update(Long id, DishCreationCommand command);

    boolean delete(Long id);

    Dish addIngredient(Long dishId, Long foodItemId, double weight);

    Dish updateIngredientWeight(Long dishId, Long foodItemId, double weight);

    Dish removeIngredient(Long dishId, Long foodItemId);
}
