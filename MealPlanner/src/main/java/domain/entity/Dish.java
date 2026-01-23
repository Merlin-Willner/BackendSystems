package domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dish {

    private Long dishId;
    private Long version;
    private Long userId;
    private String name;
    private DishCategory category;
    private double totalCost;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private double totalCalories;
    private double totalWeight;
    private double servingWeight;
    private int preparationTime;
    private String imageUrl;
    private List<DishIngredient> ingredients;

    public Dish() {
        // JPA requires a no-arg constructor
        this.ingredients = new ArrayList<>();
    }

    public Dish(Long userId, String name, DishCategory category, double servingWeight,
                int preparationTime, String imageUrl) {
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
        this.totalWeight = 0;
    }



    // Getter & Setter
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishCategory getCategory() { return category; }
    public void setCategory(DishCategory category) { this.category = category; }

    public double getTotalCost() { return totalCost; }
    public double getTotalProtein() { return totalProtein; }
    public double getTotalCarbs() { return totalCarbs; }
    public double getTotalFat() { return totalFat; }
    public double getTotalCalories() { return totalCalories; }
    public double getTotalWeight() { return calculateTotalWeight(); }

    public double getServingWeight() { return servingWeight; }
    public void setServingWeight(double servingWeight) { this.servingWeight = servingWeight; }

    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<DishIngredient> getIngredients() { return ingredients; }

    // Zutaten hinzufügen/aktualisieren/entfernen
    public void addIngredient(FoodItem foodItem, double weight) {
        if (foodItem == null) throw new IllegalArgumentException("FoodItem darf nicht null sein");
        if (foodItem.getFoodItemId() == null) throw new IllegalArgumentException("FoodItem muss eine ID besitzen");
        if (findIngredient(foodItem.getFoodItemId()).isPresent()) {
            throw new IllegalArgumentException("Zutat mit dieser FoodItem-ID existiert bereits");
        }
        DishIngredient ingredient = new DishIngredient(this, foodItem, weight);
        ingredients.add(ingredient);
        recalculateTotals();
    }

    public void updateIngredientWeight(Long foodItemId, double newWeight) {
        DishIngredient ingredient = findIngredientOrThrow(foodItemId);
        ingredient.setWeight(newWeight);
        recalculateTotals();
    }

    public void removeIngredient(Long foodItemId) {
        DishIngredient ingredient = findIngredientOrThrow(foodItemId);
        ingredients.remove(ingredient);
        recalculateTotals();
    }

    // Totals berechnen auf Basis der aktuell verknüpften FoodItems
    private void recalculateTotals() {
        totalCost = 0;
        totalProtein = 0;
        totalCarbs = 0;
        totalFat = 0;
        totalCalories = 0;
        for (DishIngredient ingredient : ingredients) {
            FoodItem foodItem = ingredient.getFoodItem();
            double weight = ingredient.getWeight();
            double factor = weight / 100.0; // Werte sind pro 100g gespeichert

            totalProtein += foodItem.getProteinPer100g() * factor;
            totalCarbs += foodItem.getCarbsPer100g() * factor;
            totalFat += foodItem.getFatPer100g() * factor;
            totalCalories += foodItem.getCaloriesPer100g() * factor;
            totalCost += foodItem.getPricePer100g() * factor;
        }
    }

    // Per-serving helpers based on servingWeight and current totalWeight.
    public double getProteinPerServing() { return scaleByServing(totalProtein); }
    public double getCarbsPerServing() { return scaleByServing(totalCarbs); }
    public double getFatPerServing() { return scaleByServing(totalFat); }
    public double getCaloriesPerServing() { return scaleByServing(totalCalories); }
    public double getCostPerServing() { return scaleByServing(totalCost); }

    private double scaleByServing(double totalValue) {
        double weight = calculateTotalWeight();
        if (weight <= 0 || servingWeight <= 0) return 0;
        return totalValue * (servingWeight / weight);
    }

    private double calculateTotalWeight() {
        if (ingredients == null || ingredients.isEmpty()) return 0;
        double sum = 0;
        for (DishIngredient ingredient : ingredients) {
            sum += ingredient.getWeight();
        }
        return sum;
    }

    private Optional<DishIngredient> findIngredient(Long foodItemId) {
        return ingredients.stream()
                .filter(i -> foodItemId != null && foodItemId.equals(i.getFoodItemId()))
                .findFirst();
    }

    private DishIngredient findIngredientOrThrow(Long foodItemId) {
        return findIngredient(foodItemId)
                .orElseThrow(() -> new IllegalArgumentException("Zutat mit FoodItem-ID " + foodItemId + " nicht gefunden"));
    }
}
