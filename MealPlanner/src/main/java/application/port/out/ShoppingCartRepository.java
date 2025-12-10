package application.port.out;

import domain.entity.ShoppingCart;
import java.util.Optional;

public interface ShoppingCartRepository {

    Optional<ShoppingCart> findById(Long id);

    ShoppingCart save(ShoppingCart cart);

}
