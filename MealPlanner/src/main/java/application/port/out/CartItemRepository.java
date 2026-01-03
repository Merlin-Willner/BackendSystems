package application.port.out;

import domain.entity.CartItem;

import java.util.Optional;

public interface CartItemRepository {
    Optional<CartItem> findById(Long id);
}
