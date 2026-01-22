package adapters.api.mapper;

import adapters.api.dto.DishIngredientResponse;
import adapters.api.dto.DishResponse;
import adapters.api.dto.FoodItemResponse;
import adapters.api.dto.ShoppingCartItemResponse;
import adapters.api.dto.ShoppingCartResponse;
import adapters.api.dto.ShoppingCartSummaryResponse;
import adapters.api.dto.UserResponse;
import application.port.in.ShoppingCartSummary;
import domain.entity.Dish;
import domain.entity.DishIngredient;
import domain.entity.FoodItem;
import domain.entity.ShoppingCart;
import domain.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public static FoodItemResponse toFoodItemResponse(FoodItem foodItem) {
        if (foodItem == null) {
            return null;
        }
        return new FoodItemResponse(
                foodItem.getFoodItemId(),
                foodItem.getName(),
                foodItem.getBrand(),
                foodItem.getPackSize(),
                foodItem.getPackPrice(),
                foodItem.getProteinPer100g(),
                foodItem.getCarbsPer100g(),
                foodItem.getFatPer100g(),
                foodItem.getCaloriesPer100g(),
                foodItem.getPricePer100g(),
                foodItem.getPricePer100gProtein(),
                foodItem.getPricePer1000Calories(),
                foodItem.getCreatedAt()
        );
    }

    public static DishResponse toDishResponse(Dish dish) {
        if (dish == null) {
            return null;
        }
        List<DishIngredientResponse> ingredients = safeDishIngredients(dish);
        return new DishResponse(
                dish.getDishId(),
                dish.getUserId(),
                dish.getName(),
                dish.getCategory(),
                dish.getTotalCost(),
                dish.getTotalProtein(),
                dish.getTotalCarbs(),
                dish.getTotalFat(),
                dish.getTotalCalories(),
                dish.getTotalWeight(),
                dish.getServingWeight(),
                dish.getPreparationTime(),
                dish.getImageUrl(),
                dish.getProteinPerServing(),
                dish.getCarbsPerServing(),
                dish.getFatPerServing(),
                dish.getCaloriesPerServing(),
                dish.getCostPerServing(),
                ingredients
        );
    }

    public static ShoppingCartResponse toShoppingCartResponse(ShoppingCart cart) {
        if (cart == null) {
            return null;
        }
        List<ShoppingCartItemResponse> items = cart.getItems() == null
                ? Collections.emptyList()
                : cart.getItems().stream()
                .filter(Objects::nonNull)
                .map(item -> new ShoppingCartItemResponse(
                        item.getFoodItemId(),
                        item.getQuantity(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());
        return new ShoppingCartResponse(
                cart.getShoppingCartId(),
                cart.getUserId(),
                items,
                cart.getTotalPrice()
        );
    }

    public static ShoppingCartSummaryResponse toSummaryResponse(ShoppingCartSummary summary) {
        if (summary == null) {
            return null;
        }
        List<ShoppingCartSummaryResponse.ItemSummaryResponse> items = summary.items() == null
                ? Collections.emptyList()
                : summary.items().stream()
                .map(item -> new ShoppingCartSummaryResponse.ItemSummaryResponse(
                        item.foodItemId(),
                        item.name(),
                        item.brand(),
                        item.packSize(),
                        item.quantity(),
                        item.packPrice(),
                        item.lineCost()
                ))
                .collect(Collectors.toList());
        return new ShoppingCartSummaryResponse(
                summary.cartId(),
                items,
                summary.totalCost()
        );
    }

    private static List<DishIngredientResponse> safeDishIngredients(Dish dish) {
        if (dish.getIngredients() == null) {
            return Collections.emptyList();
        }
        return dish.getIngredients().stream()
                .filter(Objects::nonNull)
                .map(ApiMapper::toDishIngredientResponse)
                .collect(Collectors.toList());
    }

    private static DishIngredientResponse toDishIngredientResponse(DishIngredient ingredient) {
        return new DishIngredientResponse(
                ingredient.getFoodItemId(),
                ingredient.getWeight()
        );
    }
}
