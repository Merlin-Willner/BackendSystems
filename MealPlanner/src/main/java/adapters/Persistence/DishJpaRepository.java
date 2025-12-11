package adapters.Persistence;

import application.port.out.DishRepository;
import domain.entity.Dish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Optional;

@ApplicationScoped
public class DishJpaRepository implements DishRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public Optional<Dish> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Dish.class, id));
    }
}
