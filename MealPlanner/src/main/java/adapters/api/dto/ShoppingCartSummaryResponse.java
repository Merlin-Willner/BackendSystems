package adapters.api.dto;

import java.util.List;

public record ShoppingCartSummaryResponse(
        Long cartId,
        List<ItemSummaryResponse> items,
        double totalCost
) {
    public record ItemSummaryResponse(
            Long foodItemId,
            String name,
            String brand,
            double packSize,
            int quantity,
            double packPrice,
            double lineCost
    ) {
    }
}
