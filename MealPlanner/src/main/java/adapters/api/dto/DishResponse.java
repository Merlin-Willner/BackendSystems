package adapters.api.dto;

import domain.entity.DishCategory;

import java.util.List;

public record DishResponse(
        Long dishId,
        Long userId,
        String name,
        DishCategory category,
        double totalCost,
        double totalProtein,
        double totalCarbs,
        double totalFat,
        double totalCalories,
        double totalWeight,
        double servingWeight,
        int preparationTime,
        String imageUrl,
        double proteinPerServing,
        double carbsPerServing,
        double fatPerServing,
        double caloriesPerServing,
        double costPerServing,
        List<DishIngredientResponse> ingredients
) {
}
