package domain.entity;

import java.util.UUID;

public class DishIngredient {

    private UUID dishIngredientId; // Eindeutige ID für DishIngredient
    private UUID dishId;           // Referenz zum Dish
    private UUID foodItemId;       // Referenz zum FoodItem
    private double weight;         // Gewicht in Gramm

    // Konstruktor
    public DishIngredient(UUID dishId, UUID foodItemId, double weight) {
        this.dishIngredientId = UUID.randomUUID();
        this.dishId = dishId;
        this.foodItemId = foodItemId;
        setWeight(weight); // Validierung über Setter
    }


    // Getter & Setter
    public UUID getDishIngredientId() { return dishIngredientId; }

    public void setDishIngredientId(UUID dishIngredientId) { this.dishIngredientId = dishIngredientId; }

    public UUID getDishId() { return dishId; }

    public void setDishId(UUID dishId) { this.dishId = dishId; }

    public UUID getFoodItemId() { return foodItemId; }

    public void setFoodItemId(UUID foodItemId) { this.foodItemId = foodItemId; }

    public double getWeight() { return weight; }

    public void setWeight(double weight) {
        if(weight <= 0) throw new IllegalArgumentException("Gewicht muss größer als 0 sein");
        this.weight = weight;
    }
}