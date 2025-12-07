package application.port.in;

import domain.entity.Dish;

import java.util.List;

public interface DishAPI {
    Dish create(DishCreationCommand command);

    List<Dish> findAll();

    Dish findById(Long id);
}
