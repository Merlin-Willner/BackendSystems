package adapters.API;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ShoppingCartFoodItemRequest(
        @NotNull Long foodItemId,
        @Positive Integer quantity
) {
}
