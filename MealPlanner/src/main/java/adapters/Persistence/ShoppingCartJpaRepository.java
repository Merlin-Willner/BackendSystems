package adapters.Persistence;

import application.port.out.ShoppingCartRepository;
import domain.entity.ShoppingCart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

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
    @Transactional
    public ShoppingCart save(ShoppingCart cart) {
        if(cart.getShoppingCartId() == null){
            entityManager.persist(cart);
            return cart;
        }
        return entityManager.merge(cart);
    }
}
