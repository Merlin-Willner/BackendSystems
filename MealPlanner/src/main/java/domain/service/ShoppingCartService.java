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
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.hibernate.exception.ConstraintViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        try {
            return cartRepository.save(new ShoppingCart(userId));
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new WebApplicationException("Shopping cart existiert bereits für userId " + userId, 409);
            }
            throw e;
        }
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
    @Transactional
    public ShoppingCart getCartByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new WebApplicationException("userId muss positiv sein", 400);
        }
        return cartRepository.findByUserId(userId)
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
        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new WebApplicationException("Concurrent modification detected", Response.Status.CONFLICT);
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new WebApplicationException("Shopping cart existiert bereits für userId " + userId, Response.Status.CONFLICT);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteCart(Long cartId) {
        if (cartId == null || cartId <= 0) {
            throw new WebApplicationException("cartId muss positiv sein", 400);
        }
        ShoppingCart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));
        cart.clearItems();
        try {
            cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new WebApplicationException("Concurrent modification detected", Response.Status.CONFLICT);
        }
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

    @Override
    @Transactional
    public ShoppingCart addFoodItemToCartByUser(Long userId, Long foodItemId, int quantity) {
        if (userId == null || userId <= 0) {
            throw new WebApplicationException("userId muss positiv sein", 400);
        }
        if (foodItemId == null || foodItemId <= 0) {
            throw new WebApplicationException("foodItemId muss positiv sein", 400);
        }
        if (quantity <= 0) {
            throw new WebApplicationException("quantity muss positiv sein", 400);
        }
        ShoppingCart cart = getOrCreateCart(userId);
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new NotFoundException("FoodItem not found: " + foodItemId));

        if (foodItem.getPackPrice() <= 0) {
            throw new WebApplicationException("Fooditem hat keine gültigen Werte", 422);
        }

        double totalPrice = foodItem.getPackPrice() * quantity;
        CartItem item = new CartItem(foodItemId, quantity, totalPrice);
        cart.addItem(item);

        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new WebApplicationException("Concurrent modification detected", Response.Status.CONFLICT);
        }
    }

    @Override
    @Transactional
    public ShoppingCart updateItemQuantity(Long userId, Long foodItemId, int quantity) {
        if (userId == null || userId <= 0) {
            throw new WebApplicationException("userId muss positiv sein", 400);
        }
        if (foodItemId == null || foodItemId <= 0) {
            throw new WebApplicationException("foodItemId muss positiv sein", 400);
        }
        if (quantity <= 0) {
            throw new WebApplicationException("quantity muss positiv sein", 400);
        }
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));

        boolean updated = cart.updateItemQuantity(foodItemId, quantity);
        if (!updated) {
            throw new WebApplicationException("Cart item not found", 404);
        }

        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new WebApplicationException("Concurrent modification detected", Response.Status.CONFLICT);
        }
    }

    @Override
    @Transactional
    public ShoppingCart removeItem(Long userId, Long foodItemId) {
        if (userId == null || userId <= 0) {
            throw new WebApplicationException("userId muss positiv sein", 400);
        }
        if (foodItemId == null || foodItemId <= 0) {
            throw new WebApplicationException("foodItemId muss positiv sein", 400);
        }
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));

        boolean removed = cart.removeItemByFoodItemId(foodItemId);
        if (!removed) {
            throw new WebApplicationException("Cart item not found", 404);
        }

        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new WebApplicationException("Concurrent modification detected", Response.Status.CONFLICT);
        }
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
            item.setTotalPrice(requiredPacks * foodItem.getPackPrice()); // <- Totals berechnen von Merlins Codex verbesserung
            cart.addItem(item);
        }

        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new WebApplicationException(
                    "Concurrent modification detected",
                    Response.Status.CONFLICT
            );
        }
    }

    @Transactional
    public ShoppingCart getOrCreateCart(Long userId) {
        try {
            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> cartRepository.save(new ShoppingCart(userId)));
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                return cartRepository.findByUserId(userId)
                        .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));
            }
            throw e;
        }
    }

    //UC06
    @Override
    @Transactional
    public ShoppingCartSummary getCartSummary(Long cartId) {
        ShoppingCart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new WebApplicationException("Shopping cart not found", 404));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return new ShoppingCartSummary(cart.getShoppingCartId(), List.of(), 0.0);
        }

        Map<Long, Integer> quantities = new TreeMap<>();
        for (CartItem item : cart.getItems()) {
            quantities.merge(item.getFoodItemId(), item.getQuantity(), Integer::sum);
        }

        List<ShoppingCartSummary.ItemSummary> items = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long foodItemId = entry.getKey();
            int quantity = entry.getValue();
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new WebApplicationException("FoodItem not found: " + foodItemId, 404));

            if (foodItem.getPackPrice() <= 0) {
                throw new WebApplicationException("FoodItem pack price missing: " + foodItemId, 422);
            }

            BigDecimal packPrice = BigDecimal.valueOf(foodItem.getPackPrice()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineCost = packPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
            totalCost = totalCost.add(lineCost);
            items.add(new ShoppingCartSummary.ItemSummary(
                    foodItemId,
                    foodItem.getName(),
                    foodItem.getBrand(),
                    foodItem.getPackSize(),
                    quantity,
                    packPrice.doubleValue(),
                    lineCost.doubleValue()
            ));
        }

        return new ShoppingCartSummary(cart.getShoppingCartId(), items, totalCost.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    private boolean isConstraintViolation(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof ConstraintViolationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
