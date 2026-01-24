package adapters.api.request;

import jakarta.validation.constraints.Positive;

public record DishIngredientWeightRequest(
        @Positive double weight
) {
}
