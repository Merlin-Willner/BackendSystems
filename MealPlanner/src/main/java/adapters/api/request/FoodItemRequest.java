package adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FoodItemRequest(
        @NotBlank String name,
        @NotBlank String brand,
        @Positive double packSize,
        @Positive double packPrice,
        @PositiveOrZero double proteinPer100g,
        @PositiveOrZero double carbsPer100g,
        @PositiveOrZero double fatPer100g,
        @PositiveOrZero double caloriesPer100g
) {
}
