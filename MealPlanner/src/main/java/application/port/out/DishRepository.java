package application.port.out;

import domain.entity.Dish;
import java.util.Optional;

public interface DishRepository {

    Optional<Dish> findById(Long id);
}
