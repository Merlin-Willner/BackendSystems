package adapters.Persistence;

import application.port.out.CartItemRepository;
import domain.entity.CartItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Optional;

@ApplicationScoped
public class CartItemJpaRepository implements CartItemRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public Optional<CartItem> findById(Long id) {
        return Optional.ofNullable(entityManager.find(CartItem.class, id));
    }
}
