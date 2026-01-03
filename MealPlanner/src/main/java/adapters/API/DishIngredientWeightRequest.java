package adapters.API;

import jakarta.validation.constraints.Positive;

public record DishIngredientWeightRequest(
        @Positive double weight
) {
}
