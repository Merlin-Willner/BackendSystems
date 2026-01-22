package application.port.in;

import domain.entity.ShoppingCart;

public interface ShoppingCartAPI {

    ShoppingCart createCart(Long userId);

    ShoppingCart getCartById(Long cartId);

    ShoppingCart getCartByUserId(Long userId);

    java.util.List<ShoppingCart> findAll();

    ShoppingCart updateCartUser(Long cartId, Long userId);

    void deleteCart(Long cartId);

    ShoppingCart addDishToCart(Long cartId, Long dishId, int servingsMultiplier);

    ShoppingCart addDishToCartByUser(Long userId, Long dishId, int servingsMultiplier);

    ShoppingCart addFoodItemToCartByUser(Long userId, Long foodItemId, int quantity);

    ShoppingCart updateItemQuantity(Long userId, Long foodItemId, int quantity);

    ShoppingCart removeItem(Long userId, Long foodItemId);
}
