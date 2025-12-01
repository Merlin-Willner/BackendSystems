package adapters.Persistence;

import domain.entity.FoodItem;
import application.port.out.FoodItemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class FoodItemJpaRepository implements FoodItemRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public FoodItem save(FoodItem foodItem) {
        entityManager.persist(foodItem);
        entityManager.flush(); // ID sicherstellen bevor die Domäne weiterarbeitet
        return foodItem;
    }

    @Override
    public List<FoodItem> findAll() {
        TypedQuery<FoodItem> query = entityManager.createQuery("SELECT f FROM FoodItem f", FoodItem.class);
        return query.getResultList();
    }
}
