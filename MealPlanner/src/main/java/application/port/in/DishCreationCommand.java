package application.port.in;

import domain.entity.DishCategory;

import java.util.List;

public record DishCreationCommand(
        String name,
        DishCategory category,
        double servingWeight,
        int preparationTime,
        String imageUrl,
        Long userId,
        List<IngredientCommand> ingredients
) {
    public record IngredientCommand(Long foodItemId, double weight) {}
}
