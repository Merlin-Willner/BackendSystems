package adapters.persistence;

import adapters.persistence.entity.CartItemEntity;
import adapters.persistence.entity.ShoppingCartEntity;
import adapters.persistence.mapper.PersistenceMapper;
import application.exception.ConcurrencyException;
import application.exception.ConflictException;
import application.port.out.ShoppingCartRepository;
import domain.entity.ShoppingCart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ShoppingCartJpaRepository implements ShoppingCartRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public Optional<ShoppingCart> findById(Long id) {
        ShoppingCartEntity entity = entityManager.find(ShoppingCartEntity.class, id);
        return Optional.ofNullable(PersistenceMapper.toDomain(entity));
    }

    @Override
    public Optional<ShoppingCart> findByIdWithItems(Long id) {
        try {
            TypedQuery<ShoppingCartEntity> q = entityManager.createQuery(
                    "SELECT DISTINCT c FROM ShoppingCartEntity c LEFT JOIN FETCH c.items WHERE c.shoppingCartId = :id",
                    ShoppingCartEntity.class
            );
            q.setParameter("id", id);
            return Optional.ofNullable(PersistenceMapper.toDomain(q.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public ShoppingCart save(ShoppingCart cart) {
        try {
            if (cart.getShoppingCartId() == null) {
                ShoppingCartEntity entity = new ShoppingCartEntity();
                entity.setUserId(cart.getUserId());
                entity.setTotalPrice(cart.getTotalPrice());
                List<CartItemEntity> items = PersistenceMapper.toCartItemEntities(entity, cart);
                entity.getItems().clear();
                entity.getItems().addAll(items);
                entityManager.persist(entity);
                entityManager.flush();
                return PersistenceMapper.toDomain(entity);
            }

            ShoppingCartEntity existing = entityManager.find(ShoppingCartEntity.class, cart.getShoppingCartId());
            if (existing == null) {
                ShoppingCartEntity entity = new ShoppingCartEntity();
                entity.setUserId(cart.getUserId());
                entity.setTotalPrice(cart.getTotalPrice());
                List<CartItemEntity> items = PersistenceMapper.toCartItemEntities(entity, cart);
                entity.getItems().clear();
                entity.getItems().addAll(items);
                entityManager.persist(entity);
                entityManager.flush();
                return PersistenceMapper.toDomain(entity);
            }

            existing.setUserId(cart.getUserId());
            existing.setTotalPrice(cart.getTotalPrice());
            existing.getItems().clear();
            existing.getItems().addAll(PersistenceMapper.toCartItemEntities(existing, cart));
            return PersistenceMapper.toDomain(existing);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new ConflictException("Shopping cart existiert bereits für userId " + cart.getUserId());
            }
            throw new RuntimeException("Persistence error", e);
        }
    }

    @Override
    public Optional<ShoppingCart> findByUserId(Long userId) {
        try {
            TypedQuery<ShoppingCartEntity> q = entityManager.createQuery(
                    "SELECT c FROM ShoppingCartEntity c WHERE c.userId = :userId",
                    ShoppingCartEntity.class
            );
            q.setParameter("userId", userId);

            return Optional.ofNullable(PersistenceMapper.toDomain(q.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ShoppingCart> findAll() {
        TypedQuery<ShoppingCartEntity> query = entityManager.createQuery(
                "SELECT DISTINCT c FROM ShoppingCartEntity c LEFT JOIN FETCH c.items", ShoppingCartEntity.class);
        return query.getResultList().stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void delete(ShoppingCart cart) {
        try {
            if (cart == null || cart.getShoppingCartId() == null) {
                return;
            }
            ShoppingCartEntity managed = entityManager.find(ShoppingCartEntity.class, cart.getShoppingCartId());
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
