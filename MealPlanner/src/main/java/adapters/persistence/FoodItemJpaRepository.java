package adapters.persistence;

import adapters.persistence.entity.FoodItemEntity;
import adapters.persistence.mapper.PersistenceMapper;
import application.exception.ConcurrencyException;
import application.exception.ConflictException;
import application.port.out.FoodItemRepository;
import domain.entity.FoodItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FoodItemJpaRepository implements FoodItemRepository {

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public FoodItem save(FoodItem foodItem) {
        try {
            if (foodItem.getFoodItemId() == null) {
                FoodItemEntity entity = new FoodItemEntity();
                PersistenceMapper.updateFoodItemEntity(entity, foodItem);
                if (entity.getCreatedAt() == null) {
                    entity.setCreatedAt(LocalDateTime.now());
                }
                entityManager.persist(entity);
                entityManager.flush(); // ID sicherstellen bevor die Domäne weiterarbeitet
                return PersistenceMapper.toDomain(entity);
            }
            FoodItemEntity existing = entityManager.find(FoodItemEntity.class, foodItem.getFoodItemId());
            if (existing == null) {
                FoodItemEntity entity = new FoodItemEntity();
                PersistenceMapper.updateFoodItemEntity(entity, foodItem);
                if (entity.getCreatedAt() == null) {
                    entity.setCreatedAt(LocalDateTime.now());
                }
                entityManager.persist(entity);
                entityManager.flush();
                return PersistenceMapper.toDomain(entity);
            }
            PersistenceMapper.updateFoodItemEntity(existing, foodItem);
            if (existing.getCreatedAt() == null) {
                existing.setCreatedAt(foodItem.getCreatedAt());
            }
            return PersistenceMapper.toDomain(existing);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new ConflictException("Ein FoodItem mit diesem Namen existiert bereits.");
            }
            throw new RuntimeException("Persistence error", e);
        }
    }

    //Nicht in Usecase 1 erfoderlich
    @Override
    public List<FoodItem> findAll() {
        TypedQuery<FoodItemEntity> query = entityManager.createQuery("SELECT f FROM FoodItemEntity f", FoodItemEntity.class);
        return query.getResultList().stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
    //
    @Override
    public Optional<FoodItem> findByName(String name) {
        TypedQuery<FoodItemEntity> query = entityManager.createQuery(
                "SELECT f FROM FoodItemEntity f WHERE f.name = :name", FoodItemEntity.class);
        query.setParameter("name", name);
        List<FoodItemEntity> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(PersistenceMapper.toDomain(result.getFirst()));
    }

    @Override
    public Optional<FoodItem> findById(Long id) {
        FoodItemEntity item = entityManager.find(FoodItemEntity.class, id);
        return Optional.ofNullable(PersistenceMapper.toDomain(item));
    }

    @Override
    @Transactional
    public void delete(FoodItem foodItem) {
        try {
            if (foodItem == null || foodItem.getFoodItemId() == null) {
                return;
            }
            FoodItemEntity managed = entityManager.find(FoodItemEntity.class, foodItem.getFoodItemId());
            if (managed != null) {
                entityManager.remove(managed);
            }
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            throw new RuntimeException("Persistence error", e);
        }
    }

    private boolean isConstraintViolation(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof ConstraintViolationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
