package application.port.out;

import domain.entity.ShoppingCart;
import java.util.Optional;

public interface ShoppingCartRepository {

    Optional<ShoppingCart> findById(Long id);

    Optional<ShoppingCart> findByIdWithItems(Long id);

    ShoppingCart save(ShoppingCart cart);

    Optional<ShoppingCart> findByUserId(Long userId);
}
