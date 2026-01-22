package adapters.api;

import jakarta.validation.constraints.Positive;

public record DishIngredientWeightRequest(
        @Positive double weight
) {
}
