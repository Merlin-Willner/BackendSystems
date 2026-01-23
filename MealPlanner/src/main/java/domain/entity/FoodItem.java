package domain.entity;

import java.time.LocalDateTime;
public class FoodItem {

    private Long foodItemId;
    private String name;
    private String brand;
    private double packSize;
    private double packPrice;
    private double proteinPer100g;
    private double carbsPer100g;
    private double fatPer100g;
    private double caloriesPer100g;
    private Long version;

    //Wir benötigen eine variable wo gespeicher wird wan das Fooditeam erstellt wird
    //(auch wen nicht im Diagramm erwähnt wurde) es steht so im UC01 3 Punkt drinnen : The database stores the FoodIteam with timestamps
    //es was später noch möglich ist ein Update hinzuzufügen aber die mindes anforderung sind nur ein POST
    private LocalDateTime createdAt;


    public FoodItem() {
        // JPA requires a no-arg constructor
    }

    public FoodItem(String name, String brand, double packSize, double packPrice, double proteinPer100g, double carbsPer100g, double fatPer100g, double caloriesPer100g){
        this.name = name;
        setBrand(brand);
        setPackSize(packSize);
        setPackPrice(packPrice);
        this.proteinPer100g = round(proteinPer100g);
        this.carbsPer100g = round(carbsPer100g);
        this.fatPer100g = round(fatPer100g);
        this.caloriesPer100g = round(caloriesPer100g);

        //
        this.createdAt = LocalDateTime.now();
    }

    //In den anforderugen stehth das wir alle eingaben ruden sollen auf 2 Decimalstellen UC01 punkt 2
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


    public Long getFoodItemId() { return foodItemId; }

    public void setFoodItemId(Long foodItemId) { this.foodItemId = foodItemId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }

    public void setBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand darf nicht leer sein");
        }
        this.brand = brand;
    }

    public double getPackSize() { return packSize; }

    public void setPackSize(double packSize) {
        if(packSize <= 0) throw new IllegalArgumentException("Packgröße muss > 0 sein");
        this.packSize = round(packSize);
    }

    public double getPackPrice() { return packPrice; }

    public void setPackPrice(double packPrice) {
        if(packPrice <= 0) throw new IllegalArgumentException("Preis muss > 0 sein");
        this.packPrice = round(packPrice);
    }

    public double getProteinPer100g() { return proteinPer100g; }

    public void setProteinPer100g(double proteinPer100g) {
        if(proteinPer100g < 0) throw new IllegalArgumentException("Protein darf nicht negativ sein");
        this.proteinPer100g = round(proteinPer100g);
    }

    public double getCarbsPer100g() { return carbsPer100g; }

    public void setCarbsPer100g(double carbsPer100g) {
        if(carbsPer100g < 0) throw new IllegalArgumentException("Kohlenhydrate dürfen nicht negativ sein");
        this.carbsPer100g = round(carbsPer100g);
    }

    public double getFatPer100g() { return fatPer100g; }

    public void setFatPer100g(double fatPer100g) {
        if(fatPer100g < 0) throw new IllegalArgumentException("Fett darf nicht negativ sein");
        this.fatPer100g = round(fatPer100g);
    }

    public double getCaloriesPer100g() { return caloriesPer100g; }

    public void setCaloriesPer100g(double caloriesPer100g) {
        if(caloriesPer100g < 0) throw new IllegalArgumentException("Kalorien dürfen nicht negativ sein");
        this.caloriesPer100g = round(caloriesPer100g);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Hilfsmethoden (optional)
    public double getPricePer100g() {
        return round((packPrice / packSize) * 100);
    }

    public double getPricePer100gProtein() {
        if(proteinPer100g == 0) return Double.MAX_VALUE;
        return round((getPricePer100g() / proteinPer100g) * 100);
    }

    public double getPricePer1000Calories() {
        if (caloriesPer100g == 0) return Double.MAX_VALUE;
        return round((getPricePer100g() / caloriesPer100g) * 1000);
    }
}
