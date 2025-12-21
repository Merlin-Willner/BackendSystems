package application.port.in;

import domain.entity.ShoppingCart;

public interface ShoppingCartSummaryQuery {
    ShoppingCart getCartSummary(Long cartId);
}