package application.port.in;

import domain.entity.ShoppingCart;

public interface ShoppingCartAPI {

    ShoppingCart addDishToCart(Long cartId, Long dishId, int servingsMultiplier);
}
