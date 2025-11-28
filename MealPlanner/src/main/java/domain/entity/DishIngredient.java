package domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class DishIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dishIngredientId; // Eindeutige ID für DishIngredient

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "dish_id")
    private Dish dish;             // Referenz zum Dish

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_item_id")
    private FoodIteam foodItem;    // Referenz zum FoodItem

    private double weight;         // Gewicht in Gramm

    public DishIngredient() {
        // JPA requires a no-arg constructor
    }

    // Konstruktor
    public DishIngredient(Dish dish, FoodIteam foodItem, double weight) {
        setDish(dish);
        setFoodItem(foodItem);
        setWeight(weight); // Validierung über Setter
    }


    // Getter & Setter
    public Long getDishIngredientId() { return dishIngredientId; }

    public void setDishIngredientId(Long dishIngredientId) { this.dishIngredientId = dishIngredientId; }

    public Dish getDish() { return dish; }

    public void setDish(Dish dish) {
        if (dish == null) throw new IllegalArgumentException("Dish darf nicht null sein");
        this.dish = dish;
    }

    public FoodIteam getFoodItem() { return foodItem; }

    public Long getFoodItemId() {
        return foodItem != null ? foodItem.getFoodItemId() : null;
    }

    public void setFoodItem(FoodIteam foodItem) {
        if (foodItem == null) throw new IllegalArgumentException("FoodItem darf nicht null sein");
        this.foodItem = foodItem;
    }

    public double getWeight() { return weight; }

    public void setWeight(double weight) {
        if(weight <= 0) throw new IllegalArgumentException("Gewicht muss größer als 0 sein");
        this.weight = weight;
    }
}
