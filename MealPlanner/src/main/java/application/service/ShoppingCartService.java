package application.service;


import application.exception.ConflictException;
import application.exception.ConcurrencyException;
import application.exception.NotFoundException;
import application.exception.UnprocessableException;
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
            throw new ConflictException("Shopping cart existiert bereits für userId " + userId);
        }
        try {
            return cartRepository.save(new ShoppingCart(userId));
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new ConflictException("Shopping cart existiert bereits für userId " + userId);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public ShoppingCart getCartById(Long cartId) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("cartId muss positiv sein");
        }
        return cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));
    }

    @Override
    @Transactional
    public ShoppingCart getCartByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId muss positiv sein");
        }
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));
    }

    @Override
    public List<ShoppingCart> findAll() {
        return cartRepository.findAll();
    }

    @Override
    @Transactional
    public ShoppingCart updateCartUser(Long cartId, Long userId) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("cartId muss positiv sein");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId muss positiv sein");
        }

        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));

        cartRepository.findByUserId(userId).ifPresent(existing -> {
            if (!existing.getShoppingCartId().equals(cartId)) {
                throw new ConflictException("Shopping cart existiert bereits für userId " + userId);
            }
        });

        cart.setUserId(userId);
        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new ConflictException("Shopping cart existiert bereits für userId " + userId);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteCart(Long cartId) {
        if (cartId == null || cartId <= 0) {
            throw new IllegalArgumentException("cartId muss positiv sein");
        }
        ShoppingCart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));
        cart.clearItems();
        try {
            cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
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
            throw new IllegalArgumentException("userId muss positiv sein");
        }
        if (foodItemId == null || foodItemId <= 0) {
            throw new IllegalArgumentException("foodItemId muss positiv sein");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity muss positiv sein");
        }
        ShoppingCart cart = getOrCreateCart(userId);
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new NotFoundException("FoodItem not found: " + foodItemId));

        if (foodItem.getPackPrice() <= 0) {
            throw new UnprocessableException("Fooditem hat keine gültigen Werte");
        }

        double totalPrice = foodItem.getPackPrice() * quantity;
        CartItem item = new CartItem(foodItemId, quantity, totalPrice);
        cart.addItem(item);

        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        }
    }

    @Override
    @Transactional
    public ShoppingCart updateItemQuantity(Long userId, Long foodItemId, int quantity) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId muss positiv sein");
        }
        if (foodItemId == null || foodItemId <= 0) {
            throw new IllegalArgumentException("foodItemId muss positiv sein");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity muss positiv sein");
        }
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));

        boolean updated = cart.updateItemQuantity(foodItemId, quantity);
        if (!updated) {
            throw new NotFoundException("Cart item not found");
        }

        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        }
    }

    @Override
    @Transactional
    public ShoppingCart removeItem(Long userId, Long foodItemId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId muss positiv sein");
        }
        if (foodItemId == null || foodItemId <= 0) {
            throw new IllegalArgumentException("foodItemId muss positiv sein");
        }
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));

        boolean removed = cart.removeItemByFoodItemId(foodItemId);
        if (!removed) {
            throw new NotFoundException("Cart item not found");
        }

        try {
            return cartRepository.save(cart);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        }
    }


    private ShoppingCart addDishToLoadedCart(ShoppingCart cart, Long dishId, int servingsMultiplier) {
        if (servingsMultiplier <= 0) {
            throw new IllegalArgumentException("servingsMultiplier muss positiv sein");
        }

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish not found: " + dishId));

        if (dish.getIngredients() == null || dish.getIngredients().isEmpty()) {
            throw new UnprocessableException("Dish hat keine Ingredients");
        }

        for (DishIngredient dishIngredient : dish.getIngredients()) {
            FoodItem foodItem = dishIngredient.getFoodItem();
            if (foodItem == null || foodItem.getPackPrice() <= 0 || foodItem.getPackSize() <= 0) {
                throw new UnprocessableException("Fooditem hat keine gültigen Werte");
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
            throw new ConcurrencyException("Concurrent modification detected");
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
                        .orElseThrow(() -> new NotFoundException("Shopping cart not found"));
            }
            throw e;
        }
    }

    //UC06
    @Override
    @Transactional
    public ShoppingCartSummary getCartSummary(Long cartId) {
        ShoppingCart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));

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
                    .orElseThrow(() -> new NotFoundException("FoodItem not found: " + foodItemId));

            if (foodItem.getPackPrice() <= 0) {
                throw new UnprocessableException("FoodItem pack price missing: " + foodItemId);
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
