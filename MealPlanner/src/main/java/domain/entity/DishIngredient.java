package domain.entity;

public class DishIngredient {

    /*
        Wir müssen bei der Dokumentation erwähnen, dass wir hier in DishIngredient
        einen Surrogate Key verwenden. Steht in der PDF anders drinnen.
        Da haben wir einen zusammengesetzten PK aus FoodItemID & DishID.
     */

    private Long dishIngredientId; // Eindeutige ID für DishIngredient

    private Dish dish;             // Referenz zum Dish

    private FoodItem foodItem;    // Referenz zum FoodItem

    private double weight;         // Gewicht in Gramm

    public DishIngredient() {
        // JPA requires a no-arg constructor
    }

    // Konstruktor
    public DishIngredient(Dish dish, FoodItem foodItem, double weight) {
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

    public FoodItem getFoodItem() { return foodItem; }

    public Long getFoodItemId() {
        return foodItem != null ? foodItem.getFoodItemId() : null;
    }

    public void setFoodItem(FoodItem foodItem) {
        if (foodItem == null) throw new IllegalArgumentException("FoodItem darf nicht null sein");
        this.foodItem = foodItem;
    }

    public double getWeight() { return weight; }

    public void setWeight(double weight) {
        if(weight <= 0) throw new IllegalArgumentException("Gewicht muss größer als 0 sein");
        this.weight = weight;
    }
}
