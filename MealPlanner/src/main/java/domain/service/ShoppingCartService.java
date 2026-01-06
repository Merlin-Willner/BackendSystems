package domain.service;


import application.port.in.ShoppingCartAPI;
import application.port.in.ShoppingCartSummary;
import application.port.in.ShoppingCartSummaryQuery;
import application.port.out.DishRepository;
import application.port.out.FoodItemRepository;
import application.port.out.ShoppingCartRepository;
import domain.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ShoppingCartService implements ShoppingCartAPI, ShoppingCartSummaryQuery {
    @Inject
    ShoppingCartRepository cartRepository;

    @Inject
    DishRepository dishRepository;

    @Inject
    FoodItemRepository foodItemRepository;

    @Override
    @Transactional
    public ShoppingCart createCart(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId muss positiv sein");
        }
        if (cartRepository.findByUserId(userId).isPresent()) {
            throw new WebApplicationException("Shopping cart existiert bereits für userId " + userId, 409);
        }
        return cartRepository.save(new ShoppingCart(userId));
    }

    @Override
    @Transactional
    public ShoppingCart getCartById(Long cartId) {
        if (cartId == null || cartId <= 0) {
            throw new WebApplicationException("cartId muss positiv sein", 400);
        }
        return cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));
    }

    @Override
    public List<ShoppingCart> findAll() {
        return cartRepository.findAll();
    }

    @Override
    @Transactional
    public ShoppingCart updateCartUser(Long cartId, Long userId) {
        if (cartId == null || cartId <= 0) {
            throw new WebApplicationException("cartId muss positiv sein", 400);
        }
        if (userId == null || userId <= 0) {
            throw new WebApplicationException("userId muss positiv sein", 400);
        }

        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));

        cartRepository.findByUserId(userId).ifPresent(existing -> {
            if (!existing.getShoppingCartId().equals(cartId)) {
                throw new WebApplicationException("Shopping cart existiert bereits für userId " + userId, 409);
            }
        });

        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void deleteCart(Long cartId) {
        if (cartId == null || cartId <= 0) {
            throw new WebApplicationException("cartId muss positiv sein", 400);
        }
        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));
        cartRepository.delete(cart);
    }

    //UC05
    @Override
    @Transactional
    public ShoppingCart addDishToCart(Long cartId, Long dishId, int servingsMultiplier) {
        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found: " + cartId));

        return addDishToLoadedCart(cart, dishId, servingsMultiplier);
    }

    @Override
    @Transactional
    public ShoppingCart addDishToCartByUser(Long userId, Long dishId, int servingsMultiplier) {
        ShoppingCart cart = getOrCreateCart(userId);
        return addDishToLoadedCart(cart, dishId, servingsMultiplier);
    }

    private ShoppingCart addDishToLoadedCart(ShoppingCart cart, Long dishId, int servingsMultiplier) {
        if (servingsMultiplier <= 0) {
            throw new IllegalArgumentException("servingsMultiplier muss positiv sein");
        }

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish not found: " + dishId));

        if (dish.getIngredients() == null || dish.getIngredients().isEmpty()) {
            throw new WebApplicationException("Dish hat keine Ingredients", 422);
        }

        for (DishIngredient dishIngredient : dish.getIngredients()) {
            FoodItem foodItem = dishIngredient.getFoodItem();
            if (foodItem == null || foodItem.getPackPrice() <= 0 || foodItem.getPackSize() <= 0) {
                throw new WebApplicationException("Fooditem hat keine gültigen Werte", 422);
            }

            double totalWeight = dishIngredient.getWeight() * servingsMultiplier;
            int requiredPacks = (int) Math.ceil(totalWeight / foodItem.getPackSize());
            if (requiredPacks <= 0) requiredPacks = 1;

            CartItem item = new CartItem();
            item.setFoodItemId(foodItem.getFoodItemId());
            item.setQuantity(requiredPacks);
            item.setTotalPrice(requiredPacks * foodItem.getPackPrice());

            cart.addItem(item);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public ShoppingCart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new ShoppingCart(userId)));
    }

    //UC06
    @Override
    @Transactional
    public ShoppingCartSummary getCartSummary(Long cartId) {
        ShoppingCart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new WebApplicationException("Shopping cart is empty", 422);
        }

        Map<Long, Integer> quantities = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            quantities.merge(item.getFoodItemId(), item.getQuantity(), Integer::sum);
        }

        List<ShoppingCartSummary.ItemSummary> items = new ArrayList<>();
        double totalCost = 0;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long foodItemId = entry.getKey();
            int quantity = entry.getValue();
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new WebApplicationException("FoodItem not found: " + foodItemId, 404));

            if (foodItem.getPackPrice() <= 0) {
                throw new WebApplicationException("FoodItem pack price missing: " + foodItemId, 422);
            }

            double lineCost = quantity * foodItem.getPackPrice();
            totalCost += lineCost;
            items.add(new ShoppingCartSummary.ItemSummary(
                    foodItemId,
                    quantity,
                    foodItem.getPackPrice(),
                    lineCost
            ));
        }

        return new ShoppingCartSummary(cart.getShoppingCartId(), items, totalCost);
    }
}
