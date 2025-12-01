package application.port.out;

import domain.entity.FoodItem;

public interface FoodItemRepository {
    /**
     * Persistiert ein neues FoodItem und liefert die verwaltete Instanz zurück.
     */
    FoodItem save(FoodItem foodItem);

    /**
     * Liefert alle FoodItems aus der Datenbank.
     */
    java.util.List<FoodItem> findAll();
}
