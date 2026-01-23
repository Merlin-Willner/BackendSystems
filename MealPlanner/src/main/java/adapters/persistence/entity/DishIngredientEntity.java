package adapters.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "DishIngredient")
public class DishIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dishIngredientId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dish_id")
    private DishEntity dish;

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_item_id")
    private FoodItemEntity foodItem;

    private double weight;

    public Long getDishIngredientId() {
        return dishIngredientId;
    }

    public void setDishIngredientId(Long dishIngredientId) {
        this.dishIngredientId = dishIngredientId;
    }

    public DishEntity getDish() {
        return dish;
    }

    public void setDish(DishEntity dish) {
        this.dish = dish;
    }

    public FoodItemEntity getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItemEntity foodItem) {
        this.foodItem = foodItem;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
