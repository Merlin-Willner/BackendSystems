package adapters.api.dto;

import java.time.LocalDateTime;

public record FoodItemResponse(
        Long foodItemId,
        String name,
        String brand,
        double packSize,
        double packPrice,
        double proteinPer100g,
        double carbsPer100g,
        double fatPer100g,
        double caloriesPer100g,
        double pricePer100g,
        double pricePer100gProtein,
        double pricePer1000Calories,
        LocalDateTime createdAt
) {
}
