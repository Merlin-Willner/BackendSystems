package adapters.Persistence;

import application.port.out.DishRepository;
import domain.entity.Dish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DishJpaRepository implements DishRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public Dish save(Dish dish) {
        if (dish.getDishId() == null) {
            entityManager.persist(dish);
            return dish;
        }
        return entityManager.merge(dish);
    }

    @Override
    public List<Dish> findAll() {
        TypedQuery<Dish> query = entityManager.createQuery(
                "SELECT DISTINCT d FROM Dish d LEFT JOIN FETCH d.ingredients", Dish.class);
        return query.getResultList();
    }

    @Override
    public Optional<Dish> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Dish.class, id));
    }
}
