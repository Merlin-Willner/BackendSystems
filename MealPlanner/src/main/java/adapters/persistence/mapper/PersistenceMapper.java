package adapters.persistence.mapper;

import adapters.persistence.entity.CartItemEntity;
import adapters.persistence.entity.DishEntity;
import adapters.persistence.entity.DishIngredientEntity;
import adapters.persistence.entity.FoodItemEntity;
import adapters.persistence.entity.ShoppingCartEntity;
import adapters.persistence.entity.UserEntity;
import domain.entity.CartItem;
import domain.entity.Dish;
import domain.entity.DishIngredient;
import domain.entity.FoodItem;
import domain.entity.ShoppingCart;
import domain.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    public static FoodItem toDomain(FoodItemEntity entity) {
        if (entity == null) {
            return null;
        }
        FoodItem item = new FoodItem();
        item.setFoodItemId(entity.getFoodItemId());
        item.setName(entity.getName());
        item.setBrand(entity.getBrand());
        item.setPackSize(entity.getPackSize());
        item.setPackPrice(entity.getPackPrice());
        item.setProteinPer100g(entity.getProteinPer100g());
        item.setCarbsPer100g(entity.getCarbsPer100g());
        item.setFatPer100g(entity.getFatPer100g());
        item.setCaloriesPer100g(entity.getCaloriesPer100g());
        item.setCreatedAt(entity.getCreatedAt());
        return item;
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User user = new User();
        user.setUserId(entity.getUserId());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setPasswordHash(entity.getPasswordHash());
        return user;
    }

    public static Dish toDomain(DishEntity entity) {
        if (entity == null) {
            return null;
        }
        Dish dish = new Dish(
                entity.getUserId(),
                entity.getName(),
                entity.getCategory(),
                entity.getServingWeight(),
                entity.getPreparationTime(),
                entity.getImageUrl()
        );
        dish.setDishId(entity.getDishId());
        if (entity.getIngredients() != null) {
            for (DishIngredientEntity ingredient : entity.getIngredients()) {
                FoodItem foodItem = toDomain(ingredient.getFoodItem());
                dish.addIngredient(foodItem, ingredient.getWeight());
            }
        }
        return dish;
    }

    public static ShoppingCart toDomain(ShoppingCartEntity entity) {
        if (entity == null) {
            return null;
        }
        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartId(entity.getShoppingCartId());
        cart.setUserId(entity.getUserId());
        if (entity.getItems() != null) {
            for (CartItemEntity itemEntity : entity.getItems()) {
                CartItem item = new CartItem();
                item.setCartItemId(itemEntity.getCartItemId());
                item.setFoodItemId(itemEntity.getFoodItemId());
                item.setQuantity(itemEntity.getQuantity());
                item.setTotalPrice(itemEntity.getTotalPrice());
                cart.addItem(item);
            }
        }
        return cart;
    }

    public static void updateFoodItemEntity(FoodItemEntity entity, FoodItem domain) {
        entity.setName(domain.getName());
        entity.setBrand(domain.getBrand());
        entity.setPackSize(domain.getPackSize());
        entity.setPackPrice(domain.getPackPrice());
        entity.setProteinPer100g(domain.getProteinPer100g());
        entity.setCarbsPer100g(domain.getCarbsPer100g());
        entity.setFatPer100g(domain.getFatPer100g());
        entity.setCaloriesPer100g(domain.getCaloriesPer100g());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
    }

    public static void updateUserEntity(UserEntity entity, User domain) {
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
    }

    public static void updateDishEntity(DishEntity entity, Dish domain) {
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setCategory(domain.getCategory());
        entity.setServingWeight(domain.getServingWeight());
        entity.setPreparationTime(domain.getPreparationTime());
        entity.setImageUrl(domain.getImageUrl());
        entity.setTotalCost(domain.getTotalCost());
        entity.setTotalProtein(domain.getTotalProtein());
        entity.setTotalCarbs(domain.getTotalCarbs());
        entity.setTotalFat(domain.getTotalFat());
        entity.setTotalCalories(domain.getTotalCalories());
    }

    public static List<CartItemEntity> toCartItemEntities(ShoppingCartEntity cartEntity, ShoppingCart cart) {
        if (cart.getItems() == null) {
            return List.of();
        }
        return cart.getItems().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    CartItemEntity entity = new CartItemEntity();
                    entity.setShoppingCart(cartEntity);
                    entity.setFoodItemId(item.getFoodItemId());
                    entity.setQuantity(item.getQuantity());
                    entity.setTotalPrice(item.getTotalPrice());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    public static List<DishIngredientEntity> toDishIngredientEntities(DishEntity dishEntity, Dish dish, java.util.function.Function<Long, FoodItemEntity> foodItemResolver) {
        if (dish.getIngredients() == null) {
            return List.of();
        }
        return dish.getIngredients().stream()
                .filter(Objects::nonNull)
                .map(ingredient -> {
                    DishIngredientEntity entity = new DishIngredientEntity();
                    entity.setDish(dishEntity);
                    entity.setFoodItem(foodItemResolver.apply(ingredient.getFoodItemId()));
                    entity.setWeight(ingredient.getWeight());
                    return entity;
                })
                .collect(Collectors.toList());
    }
}
