package application.port.out;

import domain.entity.FoodItem;

import java.util.List;
import java.util.Optional;

public interface FoodItemRepository {
    /**
     * Persistiert ein neues FoodItem und liefert die verwaltete Instanz zurück.
     */
    FoodItem save(FoodItem foodItem);

    /**
     * Liefert alle FoodItems aus der Datenbank.
     */
    List<FoodItem> findAll();

    //Benötigen wir da geschaut werdne muss ob es bereits den namen in der DB gibt. und Optional weil es kann vorhandne sein oder nicht
    Optional<FoodItem> findByName(String name);

    Optional<FoodItem> findById(Long id);
}
