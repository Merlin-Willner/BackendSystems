package application.port.in;

import java.util.List;

public record ShoppingCartSummary(
        Long cartId,
        List<ItemSummary> items,
        double totalCost
) {
    public record ItemSummary(
            Long foodItemId,
            int quantity,
            double packPrice,
            double lineCost
    ) {
    }
}
