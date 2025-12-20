package adapters.Persistence;

import domain.entity.FoodItem;
import application.port.out.FoodItemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

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

    //Nicht in Usecase 1 erfoderlich
    @Override
    public List<FoodItem> findAll() {
        TypedQuery<FoodItem> query = entityManager.createQuery("SELECT f FROM FoodItem f", FoodItem.class);
        return query.getResultList();
    }

    //
    @Override
    public Optional<FoodItem> findByName(String name) {
        TypedQuery<FoodItem> query = entityManager.createQuery(
                "SELECT f FROM FoodItem f WHERE f.name = :name", FoodItem.class);
        query.setParameter("name", name);
        List<FoodItem> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<FoodItem> findById(Long id) {
        FoodItem item = entityManager.find(FoodItem.class, id);
        return Optional.ofNullable(item);
    }
}
