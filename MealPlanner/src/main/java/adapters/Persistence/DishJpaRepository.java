package adapters.Persistence;

import application.port.out.DishRepository;
import domain.entity.Dish;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class DishJpaRepository implements DishRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public Optional<Dish> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Dish.class, id));
    }
}
