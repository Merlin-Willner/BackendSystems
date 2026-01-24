package adapters.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ShoppingCartCreateRequest(
        @NotNull @Positive Long userId
) {
}
