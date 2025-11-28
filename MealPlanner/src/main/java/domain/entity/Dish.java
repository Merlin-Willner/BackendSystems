package domain.entity;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Dish {

    private UUID dishId;
    private UUID userId;
    private String name;
    private DishCategory category;
    private double totalCost;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private double totalCalories;
    private double servingWeight;
    private int preparationTime;
    private String imageUrl;
    private List<DishIngredient> ingredients;

    public Dish(UUID userId, String name, DishCategory category, double servingWeight,
                int preparationTime, String imageUrl) {
        this.dishId = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.category = category != null ? category : DishCategory.OTHER;
        this.servingWeight = servingWeight;
        this.preparationTime = preparationTime;
        this.imageUrl = imageUrl;
        this.ingredients = new ArrayList<>();
        this.totalCost = 0;
        this.totalProtein = 0;
        this.totalCarbs = 0;
        this.totalFat = 0;
        this.totalCalories = 0;
    }



    // Getter & Setter
    public UUID getDishId() { return dishId; }
    public void setDishId(UUID dishId) { this.dishId = dishId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishCategory getCategory() { return category; }
    public void setCategory(DishCategory category) { this.category = category; }

    public double getTotalCost() { return totalCost; }
    public double getTotalProtein() { return totalProtein; }
    public double getTotalCarbs() { return totalCarbs; }
    public double getTotalFat() { return totalFat; }
    public double getTotalCalories() { return totalCalories; }

    public double getServingWeight() { return servingWeight; }
    public void setServingWeight(double servingWeight) { this.servingWeight = servingWeight; }

    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<DishIngredient> getIngredients() { return ingredients; }

    // Zutaten hinzufügen/entfernen
    public void addIngredient(DishIngredient ingredient) {
        if(ingredient == null) throw new IllegalArgumentException("Zutat darf nicht null sein");
        ingredients.add(ingredient);
        recalculateTotals();
    }

    public void removeIngredient(DishIngredient ingredient) {
        ingredients.remove(ingredient);
        recalculateTotals();
    }

    // Totals berechnen (Platzhalter – echte Werte über Service)
    private void recalculateTotals() {
        totalCost = 0;
        totalProtein = 0;
        totalCarbs = 0;
        totalFat = 0;
        totalCalories = 0;
        // Berechnung via FoodItemService/Repository später
    }
}