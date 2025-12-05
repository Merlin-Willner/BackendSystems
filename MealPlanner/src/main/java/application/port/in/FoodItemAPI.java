package application.port.in;

import domain.entity.FoodItem;
import java.util.List;

public interface FoodItemAPI {
    FoodItem create(FoodItem foodItem);

    List<FoodItem> findAll();

    //
    boolean existsByName(String name);
}
