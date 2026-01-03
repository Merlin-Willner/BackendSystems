package application.port.in;

import domain.entity.CartItem;
import domain.entity.ShoppingCart;

public interface ShoppingCartAPI {

    ShoppingCart createCart(Long userId);

    ShoppingCart getCartById(Long cartId);

    CartItem getCartItemById(Long cartItemId);

    ShoppingCart addDishToCart(Long cartId, Long dishId, int servingsMultiplier);

    ShoppingCart addDishToCartByUser(Long userId, Long dishId, int servingsMultiplier);
}
