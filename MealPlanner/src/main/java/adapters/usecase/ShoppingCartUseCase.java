package adapters.usecase;

import application.port.in.ShoppingCartAPI;
import application.port.in.ShoppingCartSummary;
import application.port.in.ShoppingCartSummaryQuery;
import application.port.out.DishRepository;
import application.port.out.FoodItemRepository;
import application.port.out.ShoppingCartRepository;
import application.service.ShoppingCartService;
import domain.entity.ShoppingCart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ShoppingCartUseCase implements ShoppingCartAPI, ShoppingCartSummaryQuery {

    private final ShoppingCartService service;

    @Inject
    public ShoppingCartUseCase(ShoppingCartRepository shoppingCartRepository,
                               DishRepository dishRepository,
                               FoodItemRepository foodItemRepository) {
        this.service = new ShoppingCartService(shoppingCartRepository, dishRepository, foodItemRepository);
    }

    @Override
    @Transactional
    public ShoppingCart createCart(Long userId) {
        return service.createCart(userId);
    }

    @Override
    @Transactional
    public ShoppingCart getCartById(Long cartId) {
        return service.getCartById(cartId);
    }

    @Override
    @Transactional
    public ShoppingCart getCartByUserId(Long userId) {
        return service.getCartByUserId(userId);
    }

    @Override
    public List<ShoppingCart> findAll() {
        return service.findAll();
    }

    @Override
    @Transactional
    public ShoppingCart updateCartUser(Long cartId, Long userId) {
        return service.updateCartUser(cartId, userId);
    }

    @Override
    @Transactional
    public void deleteCart(Long cartId) {
        service.deleteCart(cartId);
    }

    @Override
    @Transactional
    public ShoppingCart addDishToCart(Long cartId, Long dishId, int servingsMultiplier) {
        return service.addDishToCart(cartId, dishId, servingsMultiplier);
    }

    @Override
    @Transactional
    public ShoppingCart addDishToCartByUser(Long userId, Long dishId, int servingsMultiplier) {
        return service.addDishToCartByUser(userId, dishId, servingsMultiplier);
    }

    @Override
    @Transactional
    public ShoppingCart addFoodItemToCartByUser(Long userId, Long foodItemId, int quantity) {
        return service.addFoodItemToCartByUser(userId, foodItemId, quantity);
    }

    @Override
    @Transactional
    public ShoppingCart updateItemQuantity(Long userId, Long foodItemId, int quantity) {
        return service.updateItemQuantity(userId, foodItemId, quantity);
    }

    @Override
    @Transactional
    public ShoppingCart removeItem(Long userId, Long foodItemId) {
        return service.removeItem(userId, foodItemId);
    }

    @Override
    @Transactional
    public ShoppingCartSummary getCartSummary(Long cartId) {
        return service.getCartSummary(cartId);
    }
}
