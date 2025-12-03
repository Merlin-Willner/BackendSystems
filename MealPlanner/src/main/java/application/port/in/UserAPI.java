package application.port.in;
import domain.entity.User;
import java.util.List;


public interface UserAPI {
    User register(User user);
    User update(User user);
    User findById(Long userId);
    List<User> findAll();
}
