package adapters.api.dto;

import java.util.List;

public record ShoppingCartResponse(
        Long shoppingCartId,
        Long userId,
        List<ShoppingCartItemResponse> items,
        double totalPrice
) {
}
