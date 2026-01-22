package adapters.api.dto;

public record ShoppingCartItemResponse(
        Long foodItemId,
        int quantity,
        double totalPrice
) {
}
