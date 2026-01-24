package adapters.api.request;

import jakarta.validation.constraints.Min;

public record ShoppingCartItemUpdateRequest(
        @Min(1) int quantity
) {
}
