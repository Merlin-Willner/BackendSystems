package application.port.in;
import domain.entity.User;
import java.util.List;


public interface UserAPI {
    User register(User user);
    User update(User user);
    User findById(Long userId);
    List<User> findAll();

    User findByEmail(String email);
    User findByUsername(String username);

    boolean delete(Long userId);
}
