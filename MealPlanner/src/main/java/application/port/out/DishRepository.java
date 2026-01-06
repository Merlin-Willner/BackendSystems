package application.port.out;

import domain.entity.Dish;

import java.util.List;
import java.util.Optional;

public interface DishRepository {
    Dish save(Dish dish);

    List<Dish> findAll();

    Optional<Dish> findById(Long id);

    void delete(Dish dish);
}
