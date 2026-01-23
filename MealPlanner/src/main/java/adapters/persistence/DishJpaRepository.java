package adapters.persistence;

import adapters.persistence.entity.DishEntity;
import adapters.persistence.entity.FoodItemEntity;
import adapters.persistence.mapper.PersistenceMapper;
import application.exception.ConcurrencyException;
import application.port.out.DishRepository;
import domain.entity.Dish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DishJpaRepository implements DishRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Dish save(Dish dish) {
        try {
            if (dish.getDishId() == null) {
                DishEntity entity = new DishEntity();
                PersistenceMapper.updateDishEntity(entity, dish);
                entity.getIngredients().clear();
                entity.getIngredients().addAll(
                        PersistenceMapper.toDishIngredientEntities(
                                entity,
                                dish,
                                id -> entityManager.getReference(FoodItemEntity.class, id)
                        )
                );
                entityManager.persist(entity);
                entityManager.flush();
                return PersistenceMapper.toDomain(entity);
            }
        DishEntity existing = entityManager.find(DishEntity.class, dish.getDishId());
        if (existing == null) {
            DishEntity entity = new DishEntity();
            PersistenceMapper.updateDishEntity(entity, dish);
            entity.getIngredients().clear();
                entity.getIngredients().addAll(
                        PersistenceMapper.toDishIngredientEntities(
                                entity,
                                dish,
                                id -> entityManager.getReference(FoodItemEntity.class, id)
                        )
                );
            entityManager.persist(entity);
            entityManager.flush();
            return PersistenceMapper.toDomain(entity);
        }
        PersistenceMapper.updateDishEntity(existing, dish);
        existing.getIngredients().clear();
        entityManager.flush(); // ensure orphan removals are applied before re-insert
        existing.getIngredients().addAll(
                PersistenceMapper.toDishIngredientEntities(
                        existing,
                        dish,
                        id -> entityManager.getReference(FoodItemEntity.class, id)
                )
            );
            return PersistenceMapper.toDomain(existing);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            throw new RuntimeException("Persistence error", e);
        }
    }

    @Override
    public List<Dish> findAll() {
        TypedQuery<DishEntity> query = entityManager.createQuery(
                "SELECT DISTINCT d FROM DishEntity d LEFT JOIN FETCH d.ingredients i LEFT JOIN FETCH i.foodItem",
                DishEntity.class
        );
        return query.getResultList().stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Dish> findById(Long id) {
        TypedQuery<DishEntity> query = entityManager.createQuery(
                "SELECT DISTINCT d FROM DishEntity d LEFT JOIN FETCH d.ingredients i LEFT JOIN FETCH i.foodItem WHERE d.dishId = :id",
                DishEntity.class
        );
        query.setParameter("id", id);
        List<DishEntity> result = query.getResultList();
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(PersistenceMapper.toDomain(result.getFirst()));
    }

    @Override
    @Transactional
    public void delete(Dish dish) {
        try {
            if (dish == null || dish.getDishId() == null) {
                return;
            }
            DishEntity managed = entityManager.find(DishEntity.class, dish.getDishId());
            if (managed != null) {
                entityManager.remove(managed);
            }
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            throw new RuntimeException("Persistence error", e);
        }
    }
}
