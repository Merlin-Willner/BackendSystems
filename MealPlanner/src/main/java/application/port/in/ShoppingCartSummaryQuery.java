package application.port.in;

public interface ShoppingCartSummaryQuery {
    ShoppingCartSummary getCartSummary(Long cartId);
}
