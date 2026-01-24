package adapters.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ShoppingCartRequest(
        @NotNull Long dishId,
        @Positive Integer servingsMultiplier
) {
}
