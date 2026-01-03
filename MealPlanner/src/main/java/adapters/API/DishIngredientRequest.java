package adapters.API;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DishIngredientRequest(
        @NotNull Long foodItemId,
        @Positive double weight
) {
}
