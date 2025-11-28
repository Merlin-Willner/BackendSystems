package domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class FoodIteam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodItemId;
    private String name;
    private String brand;
    private double packSize;
    private double packPrice;
    private double proteinPer100g;
    private double carbsPer100g;
    private double fatPer100g;
    private double caloriesPer100g;

    public FoodIteam() {
        // JPA requires a no-arg constructor
    }

    public FoodIteam(String name, String brand, double packSize, double packPrice, double proteinPer100g, double carbsPer100g, double fatPer100g, double caloriesPer100g){
        this.name = name;
        this.brand = brand;
        this.packSize = packSize;
        this.packPrice = packPrice;
        this.proteinPer100g = proteinPer100g;
        this.carbsPer100g = carbsPer100g;
        this.fatPer100g = fatPer100g;
        this.caloriesPer100g = caloriesPer100g;
    }


    public Long getFoodItemId() { return foodItemId; }

    public void setFoodItemId(Long foodItemId) { this.foodItemId = foodItemId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }

    public void setBrand(String brand) { this.brand = brand; }

    public double getPackSize() { return packSize; }

    public void setPackSize(double packSize) {
        if(packSize <= 0) throw new IllegalArgumentException("Packgröße muss > 0 sein");
        this.packSize = packSize;
    }

    public double getPackPrice() { return packPrice; }

    public void setPackPrice(double packPrice) {
        if(packPrice < 0) throw new IllegalArgumentException("Preis darf nicht negativ sein");
        this.packPrice = packPrice;
    }

    public double getProteinPer100g() { return proteinPer100g; }

    public void setProteinPer100g(double proteinPer100g) {
        if(proteinPer100g < 0) throw new IllegalArgumentException("Protein darf nicht negativ sein");
        this.proteinPer100g = proteinPer100g;
    }

    public double getCarbsPer100g() { return carbsPer100g; }

    public void setCarbsPer100g(double carbsPer100g) {
        if(carbsPer100g < 0) throw new IllegalArgumentException("Kohlenhydrate dürfen nicht negativ sein");
        this.carbsPer100g = carbsPer100g;
    }

    public double getFatPer100g() { return fatPer100g; }

    public void setFatPer100g(double fatPer100g) {
        if(fatPer100g < 0) throw new IllegalArgumentException("Fett darf nicht negativ sein");
        this.fatPer100g = fatPer100g;
    }

    public double getCaloriesPer100g() { return caloriesPer100g; }

    public void setCaloriesPer100g(double caloriesPer100g) {
        if(caloriesPer100g < 0) throw new IllegalArgumentException("Kalorien dürfen nicht negativ sein");
        this.caloriesPer100g = caloriesPer100g;
    }

    // Hilfsmethoden (optional)
    public double getPricePer100g() {
        return (packPrice / packSize) * 100;
    }

    public double getPricePer100gProtein() {
        if(proteinPer100g == 0) return Double.MAX_VALUE;
        return getPricePer100g() / proteinPer100g * 100;
    }
}
