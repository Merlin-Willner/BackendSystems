package application.port.out;

import domain.entity.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    //
    Optional<User> findById(long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    void delete(User user);

}
