package application.port.in;

import domain.entity.FoodItem;
import java.util.List;

public interface FoodItemAPI {
    FoodItem create(FoodItem foodItem);

    List<FoodItem> findAll();

    //
    boolean existsByName(String name);

    //UC03
    List<FoodItem> filterAndRank(
            Double minProtein,
            Double maxProtein,
            Double minCalories,
            Double maxCalories,
            Double minFat,
            Double maxFat,
            String sortBy
    );
}
