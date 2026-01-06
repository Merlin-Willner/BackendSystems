package adapters.Persistence;

import application.port.out.ShoppingCartRepository;
import domain.entity.ShoppingCart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ShoppingCartJpaRepository implements ShoppingCartRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public Optional<ShoppingCart> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ShoppingCart.class, id));
    }

    @Override
    public Optional<ShoppingCart> findByIdWithItems(Long id) {
        try {
            TypedQuery<ShoppingCart> q = entityManager.createQuery(
                    "SELECT DISTINCT c FROM ShoppingCart c LEFT JOIN FETCH c.items WHERE c.shoppingCartId = :id",
                    ShoppingCart.class
            );
            q.setParameter("id", id);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public ShoppingCart save(ShoppingCart cart) {
        if(cart.getShoppingCartId() == null){
            entityManager.persist(cart);
            return cart;
        }
        return entityManager.merge(cart);
    }

    @Override
    public Optional<ShoppingCart> findByUserId(Long userId) {
        try {
            TypedQuery<ShoppingCart> q = entityManager.createQuery(
                    "SELECT c FROM ShoppingCart c WHERE c.userId = :userId",
                    ShoppingCart.class
            );
            q.setParameter("userId", userId);

            return Optional.of(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ShoppingCart> findAll() {
        TypedQuery<ShoppingCart> query = entityManager.createQuery(
                "SELECT DISTINCT c FROM ShoppingCart c LEFT JOIN FETCH c.items", ShoppingCart.class);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void delete(ShoppingCart cart) {
        ShoppingCart managed = entityManager.contains(cart) ? cart : entityManager.merge(cart);
        entityManager.remove(managed);
    }

}
